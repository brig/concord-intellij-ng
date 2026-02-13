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

    private static final Map<String, YamlMetaType> features = StepFeatures.combine(
            StepFeatures.nameAndMeta(), StepFeatures.error(), StepFeatures.loopAndRetry(),
            Map.of("call", CallMetaType.getInstance(),
                   "in", CallInParamsMetaType.getInstance(),
                   "out", CallOutParamsMetaType.getInstance())
    );

    private CallStepMetaType() {
        super("call", Set.of("call"));

        setDescriptionKey("doc.step.call.description");
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
