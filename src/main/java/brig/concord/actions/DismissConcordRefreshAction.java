package brig.concord.actions;

import brig.concord.dependency.DependencyChangeTracker;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * Action to dismiss the dependency reload notification.
 * The notification will reappear when dependencies change again.
 */
public final class DismissConcordRefreshAction extends DumbAwareAction {

    public DismissConcordRefreshAction() {
        super("Dismiss",
                "Hide this notification until the next change",
                AllIcons.Actions.Close);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (project == null || project.isDisposed()) {
            return;
        }

        DependencyChangeTracker.getInstance(project).dismiss();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        var project = e.getProject();
        var visible = project != null &&
                !project.isDisposed() &&
                DependencyChangeTracker.getInstance(project).needsReload();
        e.getPresentation().setEnabledAndVisible(visible);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }
}
