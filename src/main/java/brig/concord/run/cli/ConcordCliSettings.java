package brig.concord.run.cli;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "ConcordCliSettings",
        storages = @Storage("concord-cli.xml"),
        category = SettingsCategory.TOOLS
)
public final class ConcordCliSettings implements PersistentStateComponent<ConcordCliSettings> {

    private String myCliPath;
    private String myCliVersion;
    private String myJdkName;

    @Override
    public @NotNull ConcordCliSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ConcordCliSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public void noStateLoaded() {
        loadState(new ConcordCliSettings());
    }

    @Tag("cliPath")
    public @Nullable String getCliPath() {
        return myCliPath;
    }

    public void setCliPath(@Nullable String cliPath) {
        myCliPath = cliPath;
    }

    @Tag("cliVersion")
    public @Nullable String getCliVersion() {
        return myCliVersion;
    }

    public void setCliVersion(@Nullable String cliVersion) {
        myCliVersion = cliVersion;
    }

    @Tag("jdkName")
    public @Nullable String getJdkName() {
        return myJdkName;
    }

    public void setJdkName(@Nullable String jdkName) {
        myJdkName = jdkName;
    }

    public boolean isCliConfigured() {
        return myCliPath != null && !myCliPath.isBlank();
    }

    public static @NotNull ConcordCliSettings getInstance() {
        return ApplicationManager.getApplication().getService(ConcordCliSettings.class);
    }
}
