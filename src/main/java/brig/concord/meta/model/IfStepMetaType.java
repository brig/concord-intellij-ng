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

    private static final Map<String, YamlMetaType> features;

    static {
        var thenSteps = new StepsMetaType();
        thenSteps.setDescriptionKey("doc.step.feature.then.description");
        var elseSteps = new StepsMetaType();
        elseSteps.setDescriptionKey("doc.step.feature.else.description");
        features = Map.of(
                "if", ExpressionMetaType.getInstance(),
                THEN, thenSteps,
                ELSE, elseSteps,
                "meta", StepMetaMetaType.getInstance());
    }

    protected IfStepMetaType() {
        super("if", Set.of("if", THEN));

        setDescriptionKey("doc.step.if.description");
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
