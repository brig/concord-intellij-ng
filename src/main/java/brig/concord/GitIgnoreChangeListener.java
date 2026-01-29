package brig.concord;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListAdapter;
import com.intellij.openapi.vcs.changes.ChangeListListener;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;

/**
 * Listens for VCS changes and triggers UI updates when ignore status may have changed.
 * This ensures that editor notifications and inspections reflect
 * the current VCS ignore status.
 * Uses ChangeListListener instead of VFS listener to ensure that
 * ChangeListManager has already updated its cache when we refresh the UI.
 */
@Service(Service.Level.PROJECT)
public final class GitIgnoreChangeListener implements Disposable {

    private final Project project;

    public GitIgnoreChangeListener(@NotNull Project project) {
        this.project = project;

        project.getMessageBus().connect(this).subscribe(
                ChangeListListener.TOPIC,
                new ChangeListAdapter() {
                    @Override
                    public void unchangedFileStatusChanged() {
                        // Called when VCS updates ignored/unversioned file statuses
                        onVcsStatusChanged();
                    }
                }
        );
    }

    public static @NotNull GitIgnoreChangeListener getInstance(@NotNull Project project) {
        return project.getService(GitIgnoreChangeListener.class);
    }

    private void onVcsStatusChanged() {
        if (project.isDisposed()) {
            return;
        }

        // Update editor notifications (out-of-scope banners)
        EditorNotifications.getInstance(project).updateAllNotifications();

        // Restart code analysis to re-run inspections
        DaemonCodeAnalyzer.getInstance(project).restart("VCS ignore status changed");
    }

    @Override
    public void dispose() {
        // Connection is auto-disposed via connect(this)
    }
}
