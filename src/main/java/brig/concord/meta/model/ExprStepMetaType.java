package brig.concord.meta.model;

import brig.concord.meta.model.value.ExpressionMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class ExprStepMetaType extends IdentityMetaType {

    private static final ExprStepMetaType INSTANCE = new ExprStepMetaType();

    public static ExprStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = StepFeatures.combine(
            StepFeatures.nameAndMeta(), StepFeatures.error(),
            Map.of("expr", ExpressionMetaType.getInstance(),
                   "out", ExprOutParamsMetaType.getInstance())
    );

    protected ExprStepMetaType() {
        super("expr", Set.of("expr"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
