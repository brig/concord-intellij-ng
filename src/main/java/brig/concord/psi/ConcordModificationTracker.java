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
 * A modification tracker that tracks structural changes relevant to Concord scopes:
 * - VFS structural changes to Concord files (create/delete/move/rename)
 * - Changes to .gitignore files
 * - VCS ignored status changes (changelist updates)
 *
 * Note: Content changes are NOT tracked here. They are handled by PSI-based
 * caching in ConcordRoot.getPatterns() which depends on the psiFile.
 */
@Service(Service.Level.PROJECT)
public final class ConcordModificationTracker extends SimpleModificationTracker implements Disposable {

    public ConcordModificationTracker(@NotNull Project project) {
        var connection = project.getMessageBus().connect(this);

        // Track VFS changes
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                for (var event : events) {
                    if (isRelevantEvent(event)) {
                        incModificationCount();
                        return;
                    }
                }
            }
        });

        // Track VCS/gitignore changes
        ChangeListManager.getInstance(project).addChangeListListener(new ChangeListListener() {
            @Override
            public void changeListUpdateDone() {
                incModificationCount();
            }
        }, this);
    }

    public static @NotNull ConcordModificationTracker getInstance(@NotNull Project project) {
        return project.getService(ConcordModificationTracker.class);
    }

    public static @NotNull ModificationTracker tracker(@NotNull Project project) {
        return getInstance(project);
    }

    /**
     * Manually invalidates the cache. For testing purposes.
     */
    @TestOnly
    public void invalidate() {
        incModificationCount();
    }

    private boolean isRelevantEvent(@NotNull VFileEvent event) {
        // Content changes don't affect scope structure - they are tracked
        // via PSI-based caching in ConcordRoot.getPatterns()
        if (event instanceof VFileContentChangeEvent) {
            return false;
        }

        if (event instanceof VFilePropertyChangeEvent propEvent) {
            if (VirtualFile.PROP_NAME.equals(propEvent.getPropertyName())) {
                // Rename event: check both old and new names
                if (isRelevantName(propEvent.getOldValue().toString()) ||
                        isRelevantName(propEvent.getNewValue().toString())) {
                    return true;
                }
                
                // If it's a directory rename, it's always relevant as it might affect paths
                var file = event.getFile();
                return file != null && file.isDirectory();
            }
        }

        if (event instanceof VFileMoveEvent moveEvent) {
            if (isRelevantName(moveEvent.getFile().getName())) {
                return true;
            }
            // If a directory is moved, it might affect paths of concord files inside it
            return moveEvent.getFile().isDirectory();
        }

        if (event instanceof VFileDeleteEvent deleteEvent) {
            if (isRelevantName(deleteEvent.getFile().getName())) {
                return true;
            }
            return deleteEvent.getFile().isDirectory();
        }

        var fileName = getFileName(event);
        if (fileName == null) {
            return false;
        }

        return isRelevantName(fileName);
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
