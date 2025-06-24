package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class IfStepMetaType extends IdentityMetaType {

    private static final IfStepMetaType INSTANCE = new IfStepMetaType();

    public static IfStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "if", ExpressionMetaType::getInstance,
            "then", StepsMetaType::getInstance,
            "else", StepsMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance);

    protected IfStepMetaType() {
        super("If", "if", Set.of("if", "then"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
