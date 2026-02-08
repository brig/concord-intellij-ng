package brig.concord.meta.model;

import brig.concord.meta.model.value.ExpressionMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ExprStepMetaType extends IdentityMetaType {

    private static final ExprStepMetaType INSTANCE = new ExprStepMetaType();

    public static ExprStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = StepFeatures.combine(
            StepFeatures.NAME_AND_META, StepFeatures.ERROR,
            Map.of("expr", ExpressionMetaType::getInstance,
                   "out", ExprOutParamsMetaType::getInstance)
    );

    protected ExprStepMetaType() {
        super("Expr", "expr", Set.of("expr"));
    }

    @Override
    protected @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
