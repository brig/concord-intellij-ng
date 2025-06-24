package brig.concord.meta.model.call;

import brig.concord.meta.model.*;
import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CallStepMetaType extends IdentityMetaType {

    private static final CallStepMetaType INSTANCE = new CallStepMetaType();

    public static CallStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "name", StringMetaType::getInstance,
            "call", CallMetaType::getInstance,
            "in", CallInParamsMetaType::getInstance,
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
