package brig.concord.meta.model;

import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public abstract class GroupOfStepsMetaType extends IdentityMetaType {

    public static final String ERROR = "error";

    private final Map<String, YamlMetaType> features;

    protected GroupOfStepsMetaType(String name, @NotNull String identityKeyDescriptionKey) {
        super(name);

        this.features = createFeatures(name, identityKeyDescriptionKey);
    }

    protected GroupOfStepsMetaType(String name, @NotNull String identityKeyDescriptionKey, @NotNull TypeProps props) {
        super(name, props);

        this.features = createFeatures(name, identityKeyDescriptionKey);
    }

    private static Map<String, YamlMetaType> createFeatures(String name, String identityKeyDescriptionKey) {
        var identitySteps = new StepsMetaType(descKey(identityKeyDescriptionKey).andRequired());
        return StepFeatures.combine(
                StepFeatures.nameAndMeta(), StepFeatures.error(),
                Map.of(name, identitySteps,
                       "out", GroupOfStepsOutParamsMetaType.getInstance(),
                       "loop", LoopMetaType.getInstance())
        );
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
