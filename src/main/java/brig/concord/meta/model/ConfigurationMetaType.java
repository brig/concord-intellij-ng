package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.documentation.Documented;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.yaml.meta.model.YamlBooleanType;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlIntegerType;
import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConfigurationMetaType extends ConcordMetaType implements Documented {

    private static final ConfigurationMetaType INSTANCE = new ConfigurationMetaType();

    private static final Map<String, Supplier<YamlMetaType>> features = new HashMap<>();

    static {
        features.put("runtime", () -> new YamlEnumType("runtime").withLiterals("concord-v2"));
        features.put("debug", YamlBooleanType::getSharedInstance);
        features.put("entryPoint", CallMetaType::getInstance);
        features.put("dependencies", DependenciesMetaType::getInstance);
        features.put("arguments", AnyMapMetaType::getInstance);
        features.put("meta", AnyMapMetaType::getInstance);
        features.put("events", EventsMetaType::getInstance);
        features.put("requirements", AnyMapMetaType::getInstance);
        features.put("processTimeout", DurationMetaType::getInstance);
        features.put("suspendTimeout", DurationMetaType::getInstance);
        features.put("exclusive", ProcessExclusiveMetaType::getInstance);
        features.put("out", StringArrayMetaType::getInstance);
        features.put("template", StringMetaType::getInstance);
        features.put("parallelLoopParallelism", () -> YamlIntegerType.getInstance(false));
    }

    public static ConfigurationMetaType getInstance() {
        return INSTANCE;
    }

    private ConfigurationMetaType() {
        super("Configuration");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    public String getDescription() {
        return ConcordBundle.message("Configuration.description");
    }
}
