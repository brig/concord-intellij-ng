package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ScriptStepMetaType extends IdentityMetaType {

    private static final ScriptStepMetaType INSTANCE = new ScriptStepMetaType();

    public static ScriptStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "name", StringMetaType::getInstance,
            "script", StringMetaType::getInstance,
            "body", StringMetaType::getInstance,
            "in", InParamsMetaType::getInstance,
            "out", ScriptOutParamsMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance,
            "loop", LoopMetaType::getInstance,
            "retry", RetryMetaType::getInstance,
            "error", StepsMetaType::getInstance
    );

    protected ScriptStepMetaType() {
        super("Script", "script", Set.of("script"));
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
