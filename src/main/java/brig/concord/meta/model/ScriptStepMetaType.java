package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class ScriptStepMetaType extends IdentityMetaType {

    public static final String BODY_KEY = "body";
    public static final String SCRIPT_KEY = "script";

    private static final ScriptStepMetaType INSTANCE = new ScriptStepMetaType();

    public static ScriptStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = StepFeatures.combine(
            StepFeatures.nameAndMeta(), StepFeatures.error(), StepFeatures.loopAndRetry(),
            Map.of(SCRIPT_KEY, new StringMetaType().withDescriptionKey("doc.step.script.key.description"),
                   BODY_KEY, new StringMetaType().withDescriptionKey("doc.step.feature.body.description"),
                   "in", InParamsMetaType.getInstance(),
                   "out", ScriptOutParamsMetaType.getInstance())
    );

    private ScriptStepMetaType() {
        super("script", Set.of("script"));

        setDescriptionKey("doc.step.script.description");
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
