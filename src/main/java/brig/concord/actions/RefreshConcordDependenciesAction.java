package brig.concord.actions;

import brig.concord.dependency.DependencyChangeTracker;
import brig.concord.dependency.TaskRegistry;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/**
 * Action to reload Concord dependencies and task names.
 */
public class RefreshConcordDependenciesAction extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (project == null || project.isDisposed()) {
            return;
        }

        TaskRegistry.getInstance(project).reload();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (project == null || project.isDisposed()) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        boolean needsReload = DependencyChangeTracker.getInstance(project).needsReload();
        e.getPresentation().setEnabledAndVisible(needsReload);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
