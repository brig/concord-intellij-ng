package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ExprStepMetaType extends IdentityMetaType {

    private static final ExprStepMetaType INSTANCE = new ExprStepMetaType();

    public static ExprStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "expr", YamlStringType::getInstance,
            "name", YamlStringType::getInstance,
            "out", ExprOutParamsMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance,
            "error", StepsMetaType::getInstance
    );

    protected ExprStepMetaType() {
        super("Expr", "expr", Set.of("expr"));
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
