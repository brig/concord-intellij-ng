// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.concurrency.AppExecutorUtil;
import com.intellij.util.ui.update.MergingUpdateQueue;
import com.intellij.util.ui.update.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A modification tracker that tracks changes relevant to Concord scopes and dependencies.
 * Change categories:
 * - STRUCTURE_CHANGE: structural VFS changes, .gitignore changes, resources.concord changes (in root files only).
 * - DEPENDENCIES_CHANGE: dependency sections changes (root + profiles) in ANY Concord file.
 */
@Service(Service.Level.PROJECT)
public final class ConcordModificationTracker implements Disposable {

    private static final int QUEUE_DELAY_MS = 200;

    private enum ProcessingState {
        IDLE,
        COLLECTING,
        PROCESSING
    }

    private final Project project;
    private final SimpleModificationTracker structureTracker = new SimpleModificationTracker();
    private final SimpleModificationTracker dependenciesTracker = new SimpleModificationTracker();
    private final SimpleModificationTracker argumentsTracker = new SimpleModificationTracker();

    private final AtomicReference<DirtyState> dirtyRef = new AtomicReference<>(DirtyState.empty());
    private final AtomicReference<ProcessingState> state = new AtomicReference<>(ProcessingState.IDLE);

    // Unified cache for all Concord files
    private final ConcurrentMap<VirtualFile, ConcordFileFingerprint> fileCache = new ConcurrentHashMap<>();

    private final MergingUpdateQueue queue;
    private final Update update;
    private volatile boolean forceSyncInTests = false;

    public ConcordModificationTracker(@NotNull Project project) {
        this.project = project;

        var unitTestMode = ApplicationManager.getApplication().isUnitTestMode();
        var delay = unitTestMode ? 0 : QUEUE_DELAY_MS;

        this.queue = new MergingUpdateQueue(
                "concord.recalc",
                delay,
                true,
                null,
                this,
                null,
                unitTestMode
        );

        this.update = new Update("concord.recalc") {
            @Override
            public void run() {
                processBatch();
            }
        };

        var connection = project.getMessageBus().connect(this);

        connection.subscribe(VirtualFileManager.VFS_CHANGES, new ConcordVfsListener());

        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new ConcordDocumentChangeListener(), this);
    }

    public static @NotNull ConcordModificationTracker getInstance(@NotNull Project project) {
        return project.getService(ConcordModificationTracker.class);
    }

    public @NotNull ModificationTracker structure() {
        return structureTracker;
    }

    public @NotNull ModificationTracker dependencies() {
        return dependenciesTracker;
    }

    public @NotNull ModificationTracker arguments() {
        return argumentsTracker;
    }

    @TestOnly
    public void setForceSyncInTests(boolean value) {
        this.forceSyncInTests = value;
    }

    public void forceRefresh() {
        structureTracker.incModificationCount();
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!project.isDisposed()) {
                project.getMessageBus().syncPublisher(ConcordProjectListener.TOPIC).projectChanged();
            }
        });
    }

    @TestOnly
    public void invalidate() {
        structureTracker.incModificationCount();
    }

    @TestOnly
    public void invalidateDependencies() {
        dependenciesTracker.incModificationCount();
    }

    @TestOnly
    public void invalidateArguments() {
        argumentsTracker.incModificationCount();
    }

    private void onDirty(@NotNull DirtyState delta) {
        if (delta.isEmpty() || project.isDisposed()) {
            return;
        }

        if (forceSyncInTests && ApplicationManager.getApplication().isUnitTestMode()) {
            structureTracker.incModificationCount();
            dependenciesTracker.incModificationCount();
            argumentsTracker.incModificationCount();
            if (delta.vfsContentChanged() || delta.structureDirty()) {
                DaemonCodeAnalyzer.getInstance(project).restart("Concord VFS content changed (test)");
            }
            return;
        }

        dirtyRef.getAndUpdate(prev -> prev.merge(delta));

        var prevState = state.getAndUpdate(current ->
                current == ProcessingState.IDLE ? ProcessingState.COLLECTING : current
        );

        if (prevState == ProcessingState.IDLE) {
            queue.queue(update);
        }
    }

    private void processBatch() {
        if (project.isDisposed()) {
            dirtyRef.set(DirtyState.empty());
            state.set(ProcessingState.IDLE);
            return;
        }

        if (!state.compareAndSet(ProcessingState.COLLECTING, ProcessingState.PROCESSING)) {
            return;
        }

        var batch = dirtyRef.getAndSet(DirtyState.empty());
        if (batch.isEmpty()) {
            state.set(ProcessingState.IDLE);
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {

            if (project.isDisposed()) {
                state.set(ProcessingState.IDLE);
                return;
            }

            PsiDocumentManager.getInstance(project).performWhenAllCommitted(() -> {
                if (project.isDisposed()) {
                    state.set(ProcessingState.IDLE);
                    return;
                }

                var readTask = ReadAction.nonBlocking(() -> processBatchRead(batch))
                        .expireWith(this);

                if (batch.requiresSmartMode() && needsIndexScan()) {
                    readTask = readTask.inSmartMode(project);
                }

                readTask.finishOnUiThread(ModalityState.nonModal(), result -> {
                    if (project.isDisposed()) {
                        state.set(ProcessingState.IDLE);
                        return;
                    }

                    applyBatchResult(result);

                    if (dirtyRef.get().isEmpty()) {
                        state.set(ProcessingState.IDLE);
                    } else {
                        state.set(ProcessingState.COLLECTING);
                        queue.queue(update);
                    }
                }).submit(AppExecutorUtil.getAppExecutorService());
            });
        }, ModalityState.nonModal());
    }

    private boolean needsIndexScan() {
        return false;
    }

    private BatchResult processBatchRead(@NotNull DirtyState batch) {
        if (project.isDisposed()) {
            return BatchResult.empty();
        }

        var structureChanged = batch.structureDirty || batch.gitignoreDirty;
        var dependenciesChanged = false;
        var argumentsChanged = false;

        // Process all dirty files
        for (var vf : batch.dirtyFiles) {
            var isRoot = vf.isValid() && ConcordFile.isRootFileName(vf.getName());
            var isConcord = vf.isValid() && ConcordFile.isConcordFileName(vf.getName());

            if (!vf.isValid() || !isConcord) {
                var oldFp = fileCache.remove(vf);
                if (oldFp != null) {
                    // File was tracked, now invalid or not concord
                    if (oldFp.hasDependencies()) {
                        dependenciesChanged = true;
                    }
                    if (oldFp.argumentsHash() != 0) {
                        argumentsChanged = true;
                    }
                    // If it was a root file and had resources, technically structure changed,
                    // but we usually catch file deletion/rename in VFS listener already setting structureDirty=true.
                    // We can double check resources if we want to be super precise, but VFS events usually cover this.
                }
                continue;
            }

            PsiFile psiFile;
            try {
                psiFile = PsiManager.getInstance(project).findFile(vf);
            } catch (IllegalArgumentException e) {
                psiFile = null;
            }
            if (!(psiFile instanceof ConcordFile concordFile)) {
                var oldFp = fileCache.remove(vf);
                if (oldFp != null) {
                    if (oldFp.hasDependencies()) {
                        dependenciesChanged = true;
                    }
                    if (oldFp.argumentsHash() != 0) {
                        argumentsChanged = true;
                    }
                }
                continue;
            }

            var newFp = ConcordFingerprintComputer.compute(concordFile, isRoot);
            if (newFp == null) {
                var old = fileCache.remove(vf);
                if (old != null) {
                    if (old.hasDependencies()) {
                        dependenciesChanged = true;
                    }
                    if (old.argumentsHash() != 0) {
                        argumentsChanged = true;
                    }
                    if (isRoot && !old.resourcePatterns().isEmpty()) {
                        structureChanged = true;
                    }
                }
                continue;
            }

            var oldFp = fileCache.put(vf, newFp);
            if (oldFp == null) {
                // New file tracked
                if (newFp.hasDependencies()) {
                    dependenciesChanged = true;
                }
                if (newFp.argumentsHash() != 0) {
                    argumentsChanged = true;
                }
                // If it's a new root file with resources, structure changed.
                if (isRoot && !newFp.resourcePatterns().isEmpty()) {
                    structureChanged = true;
                }
            } else {
                // Existing file changed
                if (isRoot && !oldFp.resourcePatterns().equals(newFp.resourcePatterns())) {
                    structureChanged = true;
                }

                if (!oldFp.dependenciesEquals(newFp)) {
                    dependenciesChanged = true;
                }

                if (!oldFp.argumentsEquals(newFp)) {
                    argumentsChanged = true;
                }
            }
        }

        if (structureChanged || batch.cleanupNeeded) {
            var iterator = fileCache.entrySet().iterator();
            while (iterator.hasNext()) {
                var entry = iterator.next();
                var vf = entry.getKey();

                var remove = !vf.isValid() || !ConcordFile.isConcordFileName(vf.getName());

                if (remove) {
                    var fp = entry.getValue();
                    if (fp.hasDependencies()) {
                        dependenciesChanged = true;
                    }
                    if (fp.argumentsHash() != 0) {
                        argumentsChanged = true;
                    }
                    iterator.remove();
                }
            }
        }

        return new BatchResult(structureChanged, dependenciesChanged, argumentsChanged, batch.vfsContentChanged);
    }

    private void applyBatchResult(@NotNull BatchResult result) {
        if (result.structureChanged) {
            structureTracker.incModificationCount();
            project.getMessageBus().syncPublisher(ConcordProjectListener.TOPIC).projectChanged();
        }

        if (result.dependenciesChanged) {
            dependenciesTracker.incModificationCount();
            project.getMessageBus().syncPublisher(ConcordDependenciesListener.TOPIC).dependenciesChanged();
        }

        if (result.argumentsChanged) {
            argumentsTracker.incModificationCount();
        }

        if (result.vfsContentChanged || result.structureChanged) {
            DaemonCodeAnalyzer.getInstance(project).restart("Concord VFS content changed");
        }
    }

    private record BatchResult(boolean structureChanged, boolean dependenciesChanged, boolean argumentsChanged,
                               boolean vfsContentChanged) {
        private static final BatchResult EMPTY = new BatchResult(false, false, false, false);

        private static BatchResult empty() {
            return EMPTY;
        }
    }

    private final class ConcordVfsListener implements BulkFileListener {

        private boolean isInProject(@Nullable VirtualFile file, @NotNull VFileEvent event) {
            if (project.isDisposed()) {
                return false;
            }

            if (ApplicationManager.getApplication().isUnitTestMode()) {
                return true;
            }
            if (file != null && file.isValid()) {
                return ProjectFileIndex.getInstance(project).isInContent(file);
            }

            var basePath = project.getBasePath();
            return basePath != null && FileUtil.isAncestor(basePath, event.getPath(), false);
        }

        @Override
        public void after(@NotNull List<? extends VFileEvent> events) {
            var builder = new DirtyStateBuilder();

            for (var event : events) {
                var file = event.getFile();
                var resolvedFile = file != null ? file : resolveFile(event);
                if (!isInProject(resolvedFile, event)) {
                    continue;
                }
                var fileName = resolvedFile != null ? resolvedFile.getName() : getFileName(event);
                var isDirectory = isDirectoryEvent(resolvedFile, event);

                if (isDirectory) {
                    builder.structureDirty = true;
                }

                if (event instanceof VFilePropertyChangeEvent propEvent
                        && VirtualFile.PROP_NAME.equals(propEvent.getPropertyName())) {
                    var oldName = String.valueOf(propEvent.getOldValue());
                    var newName = String.valueOf(propEvent.getNewValue());

                    var oldConcord = ConcordFile.isConcordFileName(oldName);
                    var newConcord = ConcordFile.isConcordFileName(newName);
                    var oldGitignore = ".gitignore".equals(oldName);
                    var newGitignore = ".gitignore".equals(newName);

                    if (oldConcord || newConcord || oldGitignore || newGitignore) {
                        builder.structureDirty = true;
                    }
                    if (oldGitignore || newGitignore) {
                        builder.gitignoreDirty = true;
                    }

                    if (resolvedFile != null && (oldConcord || newConcord)) {
                        builder.dirtyFiles.add(resolvedFile);
                    }

                    continue;
                }

                if (isGitignoreEvent(event, resolvedFile, fileName)) {
                    builder.structureDirty = true;
                    builder.gitignoreDirty = true;
                    continue;
                }

                if (event instanceof VFileContentChangeEvent) {
                    if (resolvedFile != null && ConcordFile.isConcordFileName(resolvedFile.getName())) {
                        builder.dirtyFiles.add(resolvedFile);
                        builder.vfsContentChanged = true;
                    }
                    continue;
                }

                if (fileName != null && ConcordFile.isConcordFileName(fileName)) {
                    builder.structureDirty = true; // Added/Deleted/Moved concord file affects structure/scopes
                    if (resolvedFile != null) {
                        builder.dirtyFiles.add(resolvedFile);
                    } else {
                        builder.cleanupNeeded = true;
                    }
                }
            }

            onDirty(builder.build());
        }

        private boolean isGitignoreEvent(@NotNull VFileEvent event, @Nullable VirtualFile file, @Nullable String fileName) {
            var isGitignore = false;
            if (file != null) {
                isGitignore = ".gitignore".equals(file.getName());
            } else if (fileName != null) {
                isGitignore = ".gitignore".equals(fileName);
            } else {
                var path = event.getPath();
                isGitignore = path.endsWith("/.gitignore") || path.endsWith("\\.gitignore");
            }

            if (!isGitignore) {
                return false;
            }

            return event instanceof VFileContentChangeEvent
                    || event instanceof VFileCreateEvent
                    || event instanceof VFileDeleteEvent
                    || event instanceof VFileMoveEvent;
        }

        private @Nullable String getFileName(@NotNull VFileEvent event) {
            if (event instanceof VFileCreateEvent createEvent) {
                return createEvent.getChildName();
            }

            var path = event.getPath();
            var slash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
            if (slash < 0 || slash == path.length() - 1) {
                return null;
            }
            return path.substring(slash + 1);
        }

        private @Nullable VirtualFile resolveFile(@NotNull VFileEvent event) {
            if (event instanceof VFileCreateEvent createEvent) {
                var parent = createEvent.getParent();
                if (parent.isValid()) {
                    return parent.findChild(createEvent.getChildName());
                }
            }
            return null;
        }

        private boolean isDirectoryEvent(@Nullable VirtualFile file, @NotNull VFileEvent event) {
            if (file != null) {
                return file.isDirectory();
            }
            if (event instanceof VFileCreateEvent createEvent) {
                return createEvent.isDirectory();
            }
            return false;
        }
    }

    private final class ConcordDocumentChangeListener implements DocumentListener {

        @Override
        public void documentChanged(@NotNull DocumentEvent event) {
            if (project.isDisposed()) {
                return;
            }

            var psi = PsiDocumentManager.getInstance(project).getCachedPsiFile(event.getDocument());
            if (!(psi instanceof ConcordFile)) {
                return;
            }

            var vf = psi.getVirtualFile();
            if (vf == null) {
                return;
            }

            if (!ApplicationManager.getApplication().isUnitTestMode()
                    && !ProjectFileIndex.getInstance(project).isInContent(vf)) {
                return;
            }

            onDirty(DirtyState.create(vf));
        }
    }

    private static final class DirtyStateBuilder {
        private boolean structureDirty;
        private boolean gitignoreDirty;
        private boolean cleanupNeeded;
        private boolean vfsContentChanged;
        private final Set<VirtualFile> dirtyFiles = new HashSet<>();

        DirtyState build() {
            return new DirtyState(structureDirty, gitignoreDirty, cleanupNeeded, vfsContentChanged, dirtyFiles);
        }
    }

    private record DirtyState(
            boolean structureDirty,
            boolean gitignoreDirty,
            boolean cleanupNeeded,
            boolean vfsContentChanged,
            Set<VirtualFile> dirtyFiles
    ) {

        private DirtyState(boolean structureDirty, boolean gitignoreDirty, boolean cleanupNeeded,
                           boolean vfsContentChanged, Set<VirtualFile> dirtyFiles) {
            this.structureDirty = structureDirty;
            this.gitignoreDirty = gitignoreDirty;
            this.cleanupNeeded = cleanupNeeded;
            this.vfsContentChanged = vfsContentChanged;
            this.dirtyFiles = Set.copyOf(dirtyFiles);
        }

        static DirtyState empty() {
            return new DirtyState(false, false, false, false, Set.of());
        }

        /** Document-originated change (editor typing) — no daemon restart needed. */
        static DirtyState create(@NotNull VirtualFile file) {
            return new DirtyState(
                    false,
                    false,
                    false,
                    false,
                    Set.of(file)
            );
        }

        boolean isEmpty() {
            return !structureDirty && !gitignoreDirty && !cleanupNeeded && dirtyFiles.isEmpty();
        }

        boolean requiresSmartMode() {
            return structureDirty || gitignoreDirty;
        }

        DirtyState merge(@NotNull DirtyState other) {
            var sd = structureDirty || other.structureDirty;
            var gd = gitignoreDirty || other.gitignoreDirty;
            var cn = cleanupNeeded || other.cleanupNeeded;
            var vc = vfsContentChanged || other.vfsContentChanged;
            var files = new HashSet<>(dirtyFiles);
            files.addAll(other.dirtyFiles);
            return new DirtyState(sd, gd, cn, vc, files);
        }
    }

    @Override
    public void dispose() {
        queue.cancelAllUpdates();
        fileCache.clear();
    }
}
