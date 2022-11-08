package brig.concord.meta.model;

import brig.concord.meta.ConcordAnyMapMetaType;
import org.jetbrains.yaml.meta.model.YamlBooleanType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ParallelStepMetaType extends StepMetaType {

    private static final ParallelStepMetaType INSTANCE = new ParallelStepMetaType();

    public static ParallelStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "parallel", StepsMetaType::getInstance,
            "name", YamlStringType::getInstance,
            "out", ParallelOutParamsMetaType::getInstance,
            "meta", ConcordAnyMapMetaType::getInstance
    );

    protected ParallelStepMetaType() {
        super("Parallel", "parallel", Set.of("parallel"));
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
