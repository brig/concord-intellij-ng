package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class LogStepMetaType extends IdentityMetaType {

    private static final LogStepMetaType INSTANCE = new LogStepMetaType();

    public static LogStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "log", StringMetaType::getInstance,
            "name", StringMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance);

    protected LogStepMetaType() {
        super("Log", "log", Set.of("log"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
