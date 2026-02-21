// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class IfStepMetaType extends IdentityMetaType {

    public static final String THEN = "then";
    public static final String ELSE = "else";

    private static final IfStepMetaType INSTANCE = new IfStepMetaType();

    public static IfStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features;

    static {
        var thenSteps = new StepsMetaType(descKey("doc.step.feature.then.description").andRequired());
        var elseSteps = new StepsMetaType(descKey("doc.step.feature.else.description"));
        features = Map.of(
                "if", new ExpressionMetaType(descKey("doc.type.expression.description").andRequired()),
                THEN, thenSteps,
                ELSE, elseSteps,
                "meta", StepMetaMetaType.getInstance());
    }

    private IfStepMetaType() {
        super("if", descKey("doc.step.if.description"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
