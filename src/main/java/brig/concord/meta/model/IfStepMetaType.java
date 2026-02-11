package brig.concord.meta.model;

import brig.concord.meta.model.value.ExpressionMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class IfStepMetaType extends IdentityMetaType {

    public static final String THEN = "then";
    public static final String ELSE = "else";

    private static final IfStepMetaType INSTANCE = new IfStepMetaType();

    public static IfStepMetaType getInstance() {
        return INSTANCE;
    }

    // lazy init via holder to break circular static dependency through StepsMetaType
    private static class FeaturesHolder {
        static final Map<String, YamlMetaType> FEATURES = Map.of(
                "if", ExpressionMetaType.getInstance(),
                THEN, StepsMetaType.getInstance(),
                ELSE, StepsMetaType.getInstance(),
                "meta", StepMetaMetaType.getInstance());
    }

    protected IfStepMetaType() {
        super("if", Set.of("if", THEN));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return FeaturesHolder.FEATURES;
    }
}
