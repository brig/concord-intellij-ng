package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TriggerElementMetaType extends IdentityElementMetaType implements HighlightProvider {

    private static final List<IdentityMetaType> entries = List.of(
            new TriggerMetaType("github", GitTriggerEntryMetaType.getInstance()),
            new TriggerMetaType("cron", CronTriggerEntryMetaType.getInstance()),
            new TriggerMetaType("manual", ManualTriggerEntryMetaType.getInstance())
    );

    private static final IdentityMetaType GENERIC_TRIGGER = new IdentityMetaType("generic", Collections.emptySet()) {

        @Override
        public @Nullable Field findFeatureByName(@NotNull String name) {
            return metaTypeToField(GenericTriggerEntryMetaType.getInstance(), name);
        }

        @Override
        protected Set<String> getRequiredFields() {
            return Collections.emptySet();
        }

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            return Map.of();
        }
    };

    private static final TriggerElementMetaType INSTANCE = new TriggerElementMetaType();

    public static TriggerElementMetaType getInstance() {
        return INSTANCE;
    }

    protected TriggerElementMetaType() {
        super(entries);
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KIND;
    }

    @Override
    protected IdentityMetaType identifyEntry(Set<String> existingKeys) {
        IdentityMetaType result = super.identifyEntry(existingKeys);
        if (result != null) {
            return result;
        }

        if (existingKeys.size() == 1) {
            return GENERIC_TRIGGER;
        }

        return null;
    }

    private static class TriggerMetaType extends IdentityMetaType {

        private final Map<String, YamlMetaType> features;

        protected TriggerMetaType(String identity, YamlMetaType entry) {
            super(identity, Set.of(identity));

            this.features = Map.of(identity, entry);
        }

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            return features;
        }
    }
}
