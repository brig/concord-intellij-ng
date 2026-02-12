package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public abstract class GroupOfStepsMetaType extends IdentityMetaType {

    public static final String ERROR = "error";

    private final Map<String, YamlMetaType> features;

    protected GroupOfStepsMetaType(String name) {
        super(name, Set.of(name));

        this.features = createFeatures(name);
    }

    private static Map<String, YamlMetaType> createFeatures(String name) {
        var identitySteps = new StepsMetaType();
        identitySteps.setDescriptionKey("doc.step." + name + ".key.description");
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

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        super.validateValue(value, problemsHolder);
    }
}
