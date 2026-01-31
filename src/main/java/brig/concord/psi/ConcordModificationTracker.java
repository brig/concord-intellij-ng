package brig.concord.psi;

import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;
import com.intellij.openapi.vcs.changes.ChangeListListener;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.List;

/**
 * A modification tracker that tracks changes relevant to Concord scopes.
 * <p>
 * Two levels of tracking:
 * <ul>
 *   <li>{@link #structure()} - VFS structural changes (create/delete/move/rename), .gitignore changes</li>
 *   <li>{@link #scope()} - structural changes + granular content changes to root files (resources section)</li>
 * </ul>
 */
@Service(Service.Level.PROJECT)
public final class ConcordModificationTracker implements Disposable {

    private final SimpleModificationTracker structureTracker = new SimpleModificationTracker();
    private final SimpleModificationTracker scopeTracker = new SimpleModificationTracker();

    public ConcordModificationTracker(@NotNull Project project) {
        var connection = project.getMessageBus().connect(this);

        // Track VFS structural changes
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                var structuralChange = false;

                for (var event : events) {
                    if (isStructuralEvent(event)) {
                        structuralChange = true;
                        break;
                    }
                }

                if (structuralChange) {
                    structureTracker.incModificationCount();
                    scopeTracker.incModificationCount();
                }
            }
        });

        // Track granular content changes via PSI
        PsiManager.getInstance(project).addPsiTreeChangeListener(new PsiTreeChangeAdapter() {
            @Override
            public void childAdded(@NotNull PsiTreeChangeEvent event) {
                handlePsiChange(event);
            }

            @Override
            public void childRemoved(@NotNull PsiTreeChangeEvent event) {
                handlePsiChange(event);
            }

            @Override
            public void childReplaced(@NotNull PsiTreeChangeEvent event) {
                handlePsiChange(event);
            }

            @Override
            public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
                handlePsiChange(event);
            }
        }, this);

        // Track VCS/gitignore changes
        ChangeListManager.getInstance(project).addChangeListListener(new ChangeListListener() {
            @Override
            public void changeListUpdateDone() {
                structureTracker.incModificationCount();
                scopeTracker.incModificationCount();
            }
        }, this);
    }

    public static @NotNull ConcordModificationTracker getInstance(@NotNull Project project) {
        return project.getService(ConcordModificationTracker.class);
    }

    public @NotNull ModificationTracker structure() {
        return structureTracker;
    }

    public @NotNull ModificationTracker scope() {
        return scopeTracker;
    }

    @TestOnly
    public void invalidate() {
        structureTracker.incModificationCount();
        scopeTracker.incModificationCount();
    }

    private void handlePsiChange(@NotNull PsiTreeChangeEvent event) {
        var file = event.getFile();
        if (file == null) {
            var parent = event.getParent();
            if (parent != null && parent.isValid()) {
                file = parent.getContainingFile();
            }
        }

        if (file != null && ConcordFile.isRootFileName(file.getName())) {
            if (isRelevantPsiChange(event)) {
                scopeTracker.incModificationCount();
            }
        }
    }

    /**
     * Checks if the PSI change affects the /resources/concord section.
     * Only changes to this section affect scope (patterns).
     */
    private static boolean isRelevantPsiChange(@NotNull PsiTreeChangeEvent event) {
        var element = event.getParent();
        if (element == null || element instanceof PsiFile) {
            return true;
        }

        // Check fixed structure: .../resources/concord/...
        var concordKv = getAncestor(element, 3, YAMLKeyValue.class);
        if (concordKv == null || !"concord".equals(concordKv.getKeyText())) {
            return false;
        }

        var resourcesKv = getAncestor(concordKv, 2, YAMLKeyValue.class);
        if (resourcesKv == null || !"resources".equals(resourcesKv.getKeyText())) {
            return false;
        }

        return getAncestor(resourcesKv, 3, ConcordFile.class) != null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getAncestor(PsiElement element, int levels, Class<T> type) {
        var current = element;
        for (int i = 0; i < levels && current != null; i++) {
            current = current.getParent();
        }
        return type.isInstance(current) ? (T) current : null;
    }

    private static boolean isStructuralEvent(@NotNull VFileEvent event) {
        if (event instanceof VFileContentChangeEvent) {
            return false;
        }

        if (event instanceof VFilePropertyChangeEvent propEvent) {
            if (VirtualFile.PROP_NAME.equals(propEvent.getPropertyName())) {
                if (isRelevantName(propEvent.getOldValue().toString()) ||
                        isRelevantName(propEvent.getNewValue().toString())) {
                    return true;
                }
                var file = event.getFile();
                return file.isDirectory();
            }
        }

        if (event instanceof VFileMoveEvent moveEvent) {
            return isRelevantName(moveEvent.getFile().getName()) || moveEvent.getFile().isDirectory();
        }

        if (event instanceof VFileDeleteEvent) {
            var file = event.getFile();
            return isRelevantName(file.getName()) || file.isDirectory();
        }

        var fileName = getFileName(event);
        return fileName != null && isRelevantName(fileName);
    }

    private static boolean isRelevantName(@NotNull String name) {
        return ConcordFile.isConcordFileName(name) || ".gitignore".equals(name);
    }

    private static @Nullable String getFileName(@NotNull VFileEvent event) {
        if (event instanceof VFileCreateEvent createEvent) {
            return createEvent.getChildName();
        }
        var file = event.getFile();
        return file != null ? file.getName() : null;
    }

    @Override
    public void dispose() {}
}
