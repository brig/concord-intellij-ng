package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class LogYamlStepMetaType extends IdentityMetaType {

    private static final LogYamlStepMetaType INSTANCE = new LogYamlStepMetaType();

    public static LogYamlStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = StepFeatures.combine(
            StepFeatures.NAME_AND_META,
            Map.of("logYaml", StringMetaType::getInstance)
    );

    protected LogYamlStepMetaType() {
        super("LogYaml", "logYaml", Set.of("logYaml"));
    }

    @Override
    public @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
