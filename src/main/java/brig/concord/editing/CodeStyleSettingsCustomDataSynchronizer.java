package brig.concord.editing;

import brig.concord.ConcordLanguage;
import brig.concord.yaml.formatter.YAMLCodeStyleSettings;
import org.jetbrains.annotations.NotNull;

public class CodeStyleSettingsCustomDataSynchronizer
        extends com.intellij.psi.codeStyle.CodeStyleSettingsCustomDataSynchronizer<YAMLCodeStyleSettings> {

    @Override
    public @NotNull ConcordLanguage getLanguage() {
        return ConcordLanguage.INSTANCE;
    }

    @Override
    public @NotNull Class<YAMLCodeStyleSettings> getCustomCodeStyleSettingsClass() {
        return YAMLCodeStyleSettings.class;
    }
}
