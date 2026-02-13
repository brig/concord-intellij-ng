package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.Map;
import java.util.Set;

import static brig.concord.ConcordBundle.BUNDLE;

public abstract class GroupOfStepsMetaType extends IdentityMetaType {

    public static final String ERROR = "error";

    private final Map<String, YamlMetaType> features;

    protected GroupOfStepsMetaType(String name, @NotNull @PropertyKey(resourceBundle = BUNDLE) String identityKeyDescriptionKey) {
        super(name, Set.of(name));

        this.features = createFeatures(name, identityKeyDescriptionKey);
    }

    private static Map<String, YamlMetaType> createFeatures(String name, String identityKeyDescriptionKey) {
        var identitySteps = new StepsMetaType();
        identitySteps.setDescriptionKey(identityKeyDescriptionKey);
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
