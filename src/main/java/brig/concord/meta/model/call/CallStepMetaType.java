package brig.concord.meta.model.call;

import brig.concord.meta.model.*;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CallStepMetaType extends IdentityMetaType {

    private static final CallStepMetaType INSTANCE = new CallStepMetaType();

    public static CallStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = StepFeatures.combine(
            StepFeatures.NAME_AND_META, StepFeatures.ERROR, StepFeatures.LOOP_AND_RETRY,
            Map.of("call", CallMetaType::getInstance,
                   "in", CallInParamsMetaType::getInstance,
                   "out", CallOutParamsMetaType::getInstance)
    );

    protected CallStepMetaType() {
        super("Call", "call", Set.of("call"));
    }

    @Override
    public @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
