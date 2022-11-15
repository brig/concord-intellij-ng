package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CallStepMetaType extends IdentityMetaType {

    private static final CallStepMetaType INSTANCE = new CallStepMetaType();

    public static CallStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "name", YamlStringType::getInstance,
            "call", CallMetaType::getInstance,
            "in", InParamsMetaType::getInstance,
            "out", CallOutParamsMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance,
            "loop", LoopMetaType::getInstance,
            "retry", RetryMetaType::getInstance,
            "error", StepsMetaType::getInstance
    );

    protected CallStepMetaType() {
        super("Call", "call", Set.of("call"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
