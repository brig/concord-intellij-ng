package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class TriggerElementMetaType extends IdentityElementMetaType {

    private static final List<IdentityMetaType> entries = List.of(
            new TriggerMetaType("github", GitTriggerEntryMetaType::getInstance),
            new TriggerMetaType("cron", CronTriggerEntryMetaType::getInstance),
            new TriggerMetaType("manual", ManualTriggerEntryMetaType::getInstance)
    );

    private static final IdentityMetaType GENERIC_TRIGGER = new IdentityMetaType("generic", "generic", Collections.emptySet()) {

        @Override
        public @Nullable Field findFeatureByName(@NotNull String name) {
            return metaTypeToField(GenericTriggerEntryMetaType.getInstance(), name);
        }

        @Override
        protected Set<String> getRequiredFields() {
            return Collections.emptySet();
        }

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            return null;
        }
    };

    private static final TriggerElementMetaType INSTANCE = new TriggerElementMetaType();

    public static TriggerElementMetaType getInstance() {
        return INSTANCE;
    }

    protected TriggerElementMetaType() {
        super("Triggers", entries);
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

        private final Map<String, Supplier<YamlMetaType>> features;

        protected TriggerMetaType(String identity, Supplier<YamlMetaType> entry) {
            super(identity, identity, Set.of(identity));

            this.features = Map.of(identity, entry);
        }

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            return features;
        }
    }
}
