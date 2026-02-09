package brig.concord.meta.model;

import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLValue;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public abstract class GroupOfStepsMetaType extends IdentityMetaType {

    public static final String ERROR = "error";

    private final Map<String, Supplier<YamlMetaType>> features;

    protected GroupOfStepsMetaType(String name) {
        super(name, name, Set.of(name));

        this.features = createFeatures(name);
    }

    private static Map<String, Supplier<YamlMetaType>> createFeatures(String name) {
        return StepFeatures.combine(
                StepFeatures.NAME_AND_META, StepFeatures.ERROR,
                Map.of(name, StepsMetaType::getInstance,
                       "out", GroupOfStepsOutParamsMetaType::getInstance,
                       "loop", LoopMetaType::getInstance)
        );
    }

    @Override
    public @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        super.validateValue(value, problemsHolder);
    }
}
