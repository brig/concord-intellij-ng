package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.MapMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ProfilesMetaType extends MapMetaType implements HighlightProvider{

    private static final ProfilesMetaType INSTANCE = new ProfilesMetaType();

    public static ProfilesMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    protected YamlMetaType getMapEntryType(String name) {
        return ProfilesEntryMetaType.getInstance();
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KIND;
    }

    private static class ProfilesEntryMetaType extends ConcordMetaType implements HighlightProvider {

        private static final ProfilesEntryMetaType INSTANCE = new ProfilesEntryMetaType();

        private static final Map<String, YamlMetaType> features = Map.of(
                "configuration", ProfileConfigurationMetaType.getInstance(),
                "flows", FlowsMetaType.getInstance(),
                "forms", FormsMetaType.getInstance()
        );

        public static ProfilesEntryMetaType getInstance() {
            return INSTANCE;
        }

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            return features;
        }

        @Override
        public @Nullable TextAttributesKey getKeyHighlight(String key) {
            return ConcordHighlightingColors.DSL_SECTION;
        }
    }
}
