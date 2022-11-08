package brig.concord.meta.model;

import brig.concord.meta.ConcordAnyMapMetaType;
import org.jetbrains.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.yaml.meta.model.YamlBooleanType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class TaskStepMetaType extends StepMetaType {

    private static final TaskStepMetaType INSTANCE = new TaskStepMetaType();

    public static TaskStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "task", YamlStringType::getInstance,
            "in", InParamsMetaType::getInstance,
            "out", OutParamsMetaType::getInstance,
            "ignoreErrors", YamlBooleanType::getSharedInstance,
//            "loop", ,
            "retry", RetryMetaType::getInstance,
            "meta", ConcordAnyMapMetaType::getInstance
//            "error", ,
    );

    protected TaskStepMetaType() {
        super("Task", "task", Set.of("task"));
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}