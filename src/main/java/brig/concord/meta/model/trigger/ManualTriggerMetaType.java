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

public class ManualTriggerMetaType extends ConcordMetaType {

    private static final ManualTriggerMetaType INSTANCE = new ManualTriggerMetaType();

    public static ManualTriggerMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> requiredFields = Set.of("name", "entryPoint");

    private static final Map<String, Supplier<YamlMetaType>> features = new HashMap<>();

    static {
        features.put("name", YamlStringType::getInstance);
        features.put("entryPoint", YamlStringType::getInstance);
        features.put("activeProfiles", ConcordStringArrayMetaType::getInstance);
        features.put("exclusive", ExclusiveMetaType::getInstance);
        features.put("arguments", ConcordAnyMapMetaType::getInstance);
    }

    private ManualTriggerMetaType() {
        super("Manual");
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
