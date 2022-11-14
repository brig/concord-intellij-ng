package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;

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

    private static final TriggerElementMetaType INSTANCE = new TriggerElementMetaType();

    public static TriggerElementMetaType getInstance() {
        return INSTANCE;
    }

    protected TriggerElementMetaType() {
        super("Triggers", entries);
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
