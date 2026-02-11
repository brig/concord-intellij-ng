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

    // lazy init via holder to break circular static dependency through StepsMetaType
    private static class FeaturesHolder {
        static final Map<String, YamlMetaType> FEATURES = StepFeatures.combine(
                StepFeatures.NAME_AND_META, StepFeatures.ERROR,
                Map.of("expr", ExpressionMetaType.getInstance(),
                       "out", ExprOutParamsMetaType.getInstance())
        );
    }

    protected ExprStepMetaType() {
        super("expr", Set.of("expr"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return FeaturesHolder.FEATURES;
    }
}
