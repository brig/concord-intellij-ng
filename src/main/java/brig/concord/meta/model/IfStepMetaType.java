package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class IfStepMetaType extends IdentityMetaType {

    public static final String THEN = "then";
    public static final String ELSE = "else";

    private static final IfStepMetaType INSTANCE = new IfStepMetaType();

    public static IfStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "if", ExpressionMetaType::getInstance,
            THEN, StepsMetaType::getInstance,
            ELSE, StepsMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance);

    protected IfStepMetaType() {
        super("If", "if", Set.of("if", THEN));
    }

    @Override
    public @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
