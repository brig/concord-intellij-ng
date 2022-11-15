package brig.concord.meta.model.trigger;

import brig.concord.meta.ConcordAnyMapMetaType;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.ConcordStringArrayMetaType;
import brig.concord.meta.model.ExclusiveMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CronTriggerMetaType extends ConcordMetaType {

    private static final CronTriggerMetaType INSTANCE = new CronTriggerMetaType();

    public static CronTriggerMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> requiredFields = Set.of("spec", "entryPoint");

    private static final Map<String, Supplier<YamlMetaType>> features = new HashMap<>();

    static {
        features.put("spec", YamlStringType::getInstance);
        features.put("entryPoint", YamlStringType::getInstance);
        features.put("timezone", YamlStringType::getInstance);
        features.put("activeProfiles", ConcordStringArrayMetaType::getInstance);
        features.put("exclusive", ExclusiveMetaType::getInstance);
        features.put("arguments", ConcordAnyMapMetaType::getInstance);
    }

    private CronTriggerMetaType() {
        super("Cron");
    }

    @Override
    protected Set<String> getRequiredFields() {
        return requiredFields;
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
