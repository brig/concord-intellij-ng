package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ScriptStepMetaType extends IdentityMetaType {

    private static final ScriptStepMetaType INSTANCE = new ScriptStepMetaType();

    public static ScriptStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "name", YamlStringType::getInstance,
            "script", YamlStringType::getInstance,
            "body", YamlStringType::getInstance,
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
