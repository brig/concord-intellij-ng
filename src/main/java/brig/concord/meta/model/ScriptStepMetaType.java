package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ScriptStepMetaType extends IdentityMetaType {

    public static final String BODY_KEY = "body";
    public static final String SCRIPT_KEY = "script";

    private static final ScriptStepMetaType INSTANCE = new ScriptStepMetaType();

    public static ScriptStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = StepFeatures.combine(
            StepFeatures.NAME_AND_META, StepFeatures.ERROR, StepFeatures.LOOP_AND_RETRY,
            Map.of(SCRIPT_KEY, StringMetaType::getInstance,
                   BODY_KEY, StringMetaType::getInstance,
                   "in", InParamsMetaType::getInstance,
                   "out", ScriptOutParamsMetaType::getInstance)
    );

    protected ScriptStepMetaType() {
        super("Script", "script", Set.of("script"));
    }

    @Override
    protected @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
