// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * Application-level settings for Concord repository paths (mvn.json and depsCache).
 * The actual repository data lives in mvn.json; this only stores the paths.
 */
@State(
        name = "ConcordRepositorySettings",
        storages = @Storage("concord-repositories.xml"),
        category = SettingsCategory.TOOLS
)
public final class ConcordRepositorySettings implements PersistentStateComponent<ConcordRepositorySettings> {

    private static final String DEFAULT_MVN_JSON_PATH =
            Path.of(System.getProperty("user.home"), ".concord", "mvn.json").toString();
    private static final String DEFAULT_DEPS_CACHE_PATH =
            Path.of(System.getProperty("user.home"), ".concord", "depsCache").toString();

    private @Nullable String myDepsCachePath;

    @Override
    public @NotNull ConcordRepositorySettings getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull ConcordRepositorySettings state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @Override
    public void noStateLoaded() {
        loadState(new ConcordRepositorySettings());
    }

    @Tag("depsCachePath")
    public @Nullable String getDepsCachePath() {
        return myDepsCachePath;
    }

    public void setDepsCachePath(@Nullable String depsCachePath) {
        myDepsCachePath = depsCachePath;
    }

    /**
     * Returns the fixed mvn.json path (~/.concord/mvn.json).
     * The CLI always reads mvn.json from this location — there is no flag to override it.
     */
    public @NotNull Path getEffectiveMvnJsonPath() {
        return Path.of(DEFAULT_MVN_JSON_PATH);
    }

    /**
     * Returns the effective depsCache path, falling back to the default if not configured.
     */
    public @NotNull Path getEffectiveDepsCachePath() {
        return myDepsCachePath != null && !myDepsCachePath.isBlank()
                ? Path.of(myDepsCachePath)
                : Path.of(DEFAULT_DEPS_CACHE_PATH);
    }

    public static @NotNull String getDefaultDepsCachePath() {
        return DEFAULT_DEPS_CACHE_PATH;
    }

    public static @NotNull ConcordRepositorySettings getInstance() {
        return ApplicationManager.getApplication().getService(ConcordRepositorySettings.class);
    }
}
