package brig.concord.psi;

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
 *   <li>{@link #scope()} - structural changes + content changes to root files (patterns)</li>
 * </ul>
 */
@Service(Service.Level.PROJECT)
public final class ConcordModificationTracker implements Disposable {

    // Tracks VFS structural changes only (file list)
    private final SimpleModificationTracker structureTracker = new SimpleModificationTracker();

    // Tracks structural changes + root file content changes (patterns affect scope)
    private final SimpleModificationTracker scopeTracker = new SimpleModificationTracker();

    public ConcordModificationTracker(@NotNull Project project) {
        var connection = project.getMessageBus().connect(this);

        // Track VFS changes
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                boolean structuralChange = false;
                boolean scopeChange = false;

                for (var event : events) {
                    var eventType = classifyEvent(event);
                    if (eventType == EventType.STRUCTURAL) {
                        structuralChange = true;
                        scopeChange = true;
                        break; // Both trackers will be incremented
                    } else if (eventType == EventType.SCOPE_ONLY) {
                        scopeChange = true;
                    }
                }

                if (structuralChange) {
                    structureTracker.incModificationCount();
                }
                if (scopeChange) {
                    scopeTracker.incModificationCount();
                }
            }
        });

        // Track VCS/gitignore changes - affects both structure and scope
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

    /**
     * Tracker for structural changes only (file additions/deletions/renames).
     * Use for caches that only depend on the file list, not on file contents.
     */
    public @NotNull ModificationTracker structure() {
        return structureTracker;
    }

    /**
     * Tracker for scope changes (structural + root file content changes).
     * Use for caches that depend on patterns from root files.
     */
    public @NotNull ModificationTracker scope() {
        return scopeTracker;
    }

    /**
     * @deprecated Use {@link #structure()} or {@link #scope()} instead.
     */
    @Deprecated
    public static @NotNull ModificationTracker tracker(@NotNull Project project) {
        return getInstance(project).scope();
    }

    /**
     * Manually invalidates all caches. For testing purposes.
     */
    @TestOnly
    public void invalidate() {
        structureTracker.incModificationCount();
        scopeTracker.incModificationCount();
    }

    private enum EventType {
        NONE,
        SCOPE_ONLY,    // Only affects scope (e.g., root file content change)
        STRUCTURAL     // Affects both structure and scope
    }

    private @NotNull EventType classifyEvent(@NotNull VFileEvent event) {
        // Content changes to root files affect scope (patterns may change)
        switch (event) {
            case VFileContentChangeEvent contentEvent -> {
                var file = contentEvent.getFile();
                if (ConcordFile.isRootFileName(file.getName())) {
                    return EventType.SCOPE_ONLY;
                }
                return EventType.NONE;
            }
            case VFilePropertyChangeEvent propEvent -> {
                if (VirtualFile.PROP_NAME.equals(propEvent.getPropertyName())) {
                    // Rename event: check both old and new names
                    if (isRelevantName(propEvent.getOldValue().toString()) ||
                            isRelevantName(propEvent.getNewValue().toString())) {
                        return EventType.STRUCTURAL;
                    }

                    // If it's a directory rename, it's always relevant as it might affect paths
                    var file = event.getFile();
                    if (file.isDirectory()) {
                        return EventType.STRUCTURAL;
                    }
                }
                return EventType.NONE;
            }
            case VFileMoveEvent moveEvent -> {
                if (isRelevantName(moveEvent.getFile().getName())) {
                    return EventType.STRUCTURAL;
                }
                // If a directory is moved, it might affect paths of concord files inside it
                if (moveEvent.getFile().isDirectory()) {
                    return EventType.STRUCTURAL;
                }
                return EventType.NONE;
            }
            case VFileDeleteEvent deleteEvent -> {
                if (isRelevantName(deleteEvent.getFile().getName())) {
                    return EventType.STRUCTURAL;
                }
                if (deleteEvent.getFile().isDirectory()) {
                    return EventType.STRUCTURAL;
                }
                return EventType.NONE;
            }
            default -> {
            }
        }

        var fileName = getFileName(event);
        if (fileName == null) {
            return EventType.NONE;
        }

        return isRelevantName(fileName) ? EventType.STRUCTURAL : EventType.NONE;
    }

    private boolean isRelevantName(@NotNull String name) {
        return ConcordFile.isConcordFileName(name) || ".gitignore".equals(name);
    }

    private @Nullable String getFileName(@NotNull VFileEvent event) {
        if (event instanceof VFileCreateEvent createEvent) {
            return createEvent.getChildName();
        }

        var file = event.getFile();
        if (file != null) {
            return file.getName();
        }

        return null;
    }

    @Override
    public void dispose() {
        // Connection is disposed automatically via Disposable parent
    }
}
