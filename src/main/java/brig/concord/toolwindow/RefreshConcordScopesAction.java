package brig.concord.toolwindow;

import brig.concord.psi.ConcordModificationTracker;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class RefreshConcordScopesAction extends AnAction {

    public RefreshConcordScopesAction() {
        super("Refresh", "Refresh Concord scopes", AllIcons.Actions.Refresh);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        var project = e.getProject();
        if (project == null) {
            return;
        }
        ConcordModificationTracker.getInstance(project).forceRefresh();
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
