package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public abstract class GroupOfStepsMetaType extends StepMetaType {

    private final Map<String, Supplier<YamlMetaType>> features;

    protected GroupOfStepsMetaType(String name) {
        super(name, name, Set.of(name));

        this.features = createFeatures(name);
    }

    private static Map<String, Supplier<YamlMetaType>> createFeatures(String name) {
        return Map.of(
                "name", YamlStringType::getInstance,
                name, StepsMetaType::getInstance,
                "out", GroupOfStepsOutParamsMetaType::getInstance,
                "meta", StepMetaMetaType::getInstance,
                "loop", LoopMetaType::getInstance,
                "error", StepsMetaType::getInstance
        );
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
