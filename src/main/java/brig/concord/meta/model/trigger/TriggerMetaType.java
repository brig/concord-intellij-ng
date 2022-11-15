package brig.concord.meta.model.trigger;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.function.Supplier;

public class TriggerMetaType extends ConcordMetaType {

    private static final TriggerMetaType INSTANCE = new TriggerMetaType();

    public static TriggerMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "gitHub", GitHubTriggerMetaType::getInstance,
            "cron", CronTriggerMetaType::getInstance,
            "manual", ManualTriggerMetaType::getInstance
    );

    public TriggerMetaType() {
        super("Trigger");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
