package brig.concord.meta.model;

import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ExprStepMetaType extends IdentityMetaType {

    private static final ExprStepMetaType INSTANCE = new ExprStepMetaType();

    public static ExprStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = StepFeatures.combine(
            StepFeatures.nameAndMeta(), StepFeatures.error(),
            Map.of("expr", new ExpressionMetaType(descKey("doc.type.expression.description").andRequired()),
                   "out", ExprOutParamsMetaType.getInstance())
    );

    private ExprStepMetaType() {
        super("expr", descKey("doc.step.expr.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
