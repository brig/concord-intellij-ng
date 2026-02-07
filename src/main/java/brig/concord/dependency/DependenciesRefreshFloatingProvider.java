package brig.concord.dependency;

import brig.concord.ConcordFileType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Provides a floating toolbar in Concord editors when dependencies need to be reloaded.
 * Shows Refresh and Dismiss actions when dependency changes are detected.
 */
public class DependenciesRefreshFloatingProvider implements FloatingToolbarProvider {

    @Override
    public boolean isApplicable(@NotNull DataContext dataContext) {
        var project = dataContext.getData(CommonDataKeys.PROJECT);
        if (project == null || project.isDisposed()) {
            return false;
        }

        // Always applicable for Concord files; visibility is controlled by Action.update()
        var file = dataContext.getData(CommonDataKeys.VIRTUAL_FILE);
        return file != null && ConcordFileType.INSTANCE.equals(file.getFileType());
    }

    @Override
    public @NotNull ActionGroup getActionGroup() {
        var am = ActionManager.getInstance();

        var refresh = am.getAction("Concord.RefreshProject");
        var dismiss = am.getAction("Concord.Refresh.Dismiss");

        DefaultActionGroup g = new DefaultActionGroup();
        if (refresh != null) {
            g.add(refresh);
        }
        if (dismiss != null) {
            g.add(dismiss);
        }
        return g;
    }

    @Override
    public boolean getAutoHideable() {
        return true;
    }
}
