// SPDX-License-Identifier: Apache-2.0
package brig.concord.run;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import com.intellij.util.xmlb.annotations.XMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service(Service.Level.PROJECT)
@State(
        name = "ConcordRunModeSettings",
        storages = @Storage("concord-run-mode.xml"),
        category = SettingsCategory.TOOLS
)
public final class ConcordRunModeSettings implements PersistentStateComponent<ConcordRunModeSettings> {

    public enum RunMode {
        DIRECT,
        DELEGATING
    }

    private RunMode myRunMode = RunMode.DIRECT;
    private String myMainEntryPoint = "default";
    private String myFlowParameterName = "flow";
    private List<String> myActiveProfiles = new ArrayList<>();
    private Map<String, String> myDefaultParameters = new LinkedHashMap<>();

    @Override
    public @NotNull ConcordRunModeSettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ConcordRunModeSettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public void noStateLoaded() {
        loadState(new ConcordRunModeSettings());
    }

    @Tag("runMode")
    public @NotNull RunMode getRunMode() {
        return myRunMode;
    }

    public void setRunMode(@NotNull RunMode runMode) {
        myRunMode = runMode;
    }

    @Tag("mainEntryPoint")
    public @NotNull String getMainEntryPoint() {
        return myMainEntryPoint;
    }

    public void setMainEntryPoint(@NotNull String mainEntryPoint) {
        myMainEntryPoint = mainEntryPoint;
    }

    @Tag("flowParameterName")
    public @NotNull String getFlowParameterName() {
        return myFlowParameterName;
    }

    public void setFlowParameterName(@NotNull String flowParameterName) {
        myFlowParameterName = flowParameterName;
    }

    @Tag("activeProfiles")
    @XCollection(elementName = "profile")
    public @NotNull List<String> getActiveProfiles() {
        return new ArrayList<>(myActiveProfiles);
    }

    public void setActiveProfiles(@NotNull List<String> activeProfiles) {
        myActiveProfiles = new ArrayList<>(activeProfiles);
    }

    @Tag("defaultParameters")
    @XMap(entryTagName = "param", keyAttributeName = "key", valueAttributeName = "value")
    public @NotNull Map<String, String> getDefaultParameters() {
        return new LinkedHashMap<>(myDefaultParameters);
    }

    public void setDefaultParameters(@NotNull Map<String, String> defaultParameters) {
        myDefaultParameters = new LinkedHashMap<>(defaultParameters);
    }

    public boolean isDelegatingMode() {
        return myRunMode == RunMode.DELEGATING;
    }

    public static @NotNull ConcordRunModeSettings getInstance(@NotNull Project project) {
        return project.getService(ConcordRunModeSettings.class);
    }
}
