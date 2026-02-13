package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class ScriptStepMetaType extends IdentityMetaType {

    public static final String BODY_KEY = "body";
    public static final String SCRIPT_KEY = "script";

    private static final ScriptStepMetaType INSTANCE = new ScriptStepMetaType();

    public static ScriptStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = StepFeatures.combine(
            StepFeatures.nameAndMeta(), StepFeatures.error(), StepFeatures.loopAndRetry(),
            Map.of(SCRIPT_KEY, new StringMetaType(desc("doc.step.script.key.description").andRequired()),
                   BODY_KEY, new StringMetaType(desc("doc.step.feature.body.description")),
                   "in", InParamsMetaType.getInstance(),
                   "out", ScriptOutParamsMetaType.getInstance())
    );

    private ScriptStepMetaType() {
        super("script", desc("doc.step.script.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
