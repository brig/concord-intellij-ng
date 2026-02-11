package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class LogYamlStepMetaType extends IdentityMetaType {

    private static final LogYamlStepMetaType INSTANCE = new LogYamlStepMetaType();

    public static LogYamlStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = StepFeatures.combine(
            StepFeatures.nameAndMeta(),
            Map.of("logYaml", StringMetaType.getInstance())
    );

    protected LogYamlStepMetaType() {
        super("logYaml", Set.of("logYaml"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
