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

    private volatile Map<String, YamlMetaType> features;

    protected ScriptStepMetaType() {
        super("script", Set.of("script"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        var f = features;
        if (f == null) {
            f = StepFeatures.combine(
                    StepFeatures.nameAndMeta(), StepFeatures.error(), StepFeatures.loopAndRetry(),
                    Map.of(SCRIPT_KEY, StringMetaType.getInstance(),
                           BODY_KEY, StringMetaType.getInstance(),
                           "in", InParamsMetaType.getInstance(),
                           "out", ScriptOutParamsMetaType.getInstance())
            );
            features = f;
        }
        return f;
    }
}
