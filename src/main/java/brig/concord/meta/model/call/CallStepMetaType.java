package brig.concord.meta.model.call;

import brig.concord.meta.model.IdentityMetaType;
import brig.concord.meta.model.StepFeatures;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class CallStepMetaType extends IdentityMetaType {

    private static final CallStepMetaType INSTANCE = new CallStepMetaType();

    public static CallStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = StepFeatures.combine(
            StepFeatures.nameAndMeta(), StepFeatures.error(), StepFeatures.loopAndRetry(),
            Map.of("call", new CallMetaType(desc("doc.step.call.key.description").andRequired()),
                   "in", CallInParamsMetaType.getInstance(),
                   "out", CallOutParamsMetaType.getInstance())
    );

    private CallStepMetaType() {
        super("call", desc("doc.step.call.description"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
