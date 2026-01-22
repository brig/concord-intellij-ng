package brig.concord.ui.status;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.openapi.wm.StatusBarWidgetFactory;
import org.jetbrains.annotations.NotNull;

public final class ConcordStatusWidgetFactory
        implements StatusBarWidgetFactory {

    @Override
    public @NotNull String getId() {
        return "ConcordStatusWidget";
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Concord Status";
    }

    @Override
    public boolean isAvailable(@NotNull Project project) {
        // можно скрывать, если не Concord-проект
        return true;
    }

    @Override
    public @NotNull StatusBarWidget createWidget(@NotNull Project project) {
        return new ConcordStatusWidget(project);
    }

    @Override
    public void disposeWidget(@NotNull StatusBarWidget widget) {
        widget.dispose();
    }

    @Override
    public boolean canBeEnabledOn(@NotNull StatusBar statusBar) {
        return true;
    }
}
