package brig.concord.meta.model.call;

import brig.concord.meta.model.*;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class CallStepMetaType extends IdentityMetaType {

    private static final CallStepMetaType INSTANCE = new CallStepMetaType();

    public static CallStepMetaType getInstance() {
        return INSTANCE;
    }

    // lazy init via holder to break circular static dependency through StepsMetaType
    private static class FeaturesHolder {
        static final Map<String, YamlMetaType> FEATURES = StepFeatures.combine(
                StepFeatures.NAME_AND_META, StepFeatures.ERROR, StepFeatures.LOOP_AND_RETRY,
                Map.of("call", CallMetaType.getInstance(),
                       "in", CallInParamsMetaType.getInstance(),
                       "out", CallOutParamsMetaType.getInstance())
        );
    }

    protected CallStepMetaType() {
        super("Call", "call", Set.of("call"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return FeaturesHolder.FEATURES;
    }
}
