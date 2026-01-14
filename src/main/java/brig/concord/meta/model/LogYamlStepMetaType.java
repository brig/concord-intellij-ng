package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class LogYamlStepMetaType extends IdentityMetaType {

    private static final LogYamlStepMetaType INSTANCE = new LogYamlStepMetaType();

    public static LogYamlStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "logYaml", StringMetaType::getInstance,
            "name", StepNameMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance);

    protected LogYamlStepMetaType() {
        super("LogYaml", "logYaml", Set.of("logYaml"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
