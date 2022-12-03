package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class ParallelStepMetaType extends IdentityMetaType {

    private static final ParallelStepMetaType INSTANCE = new ParallelStepMetaType();

    public static ParallelStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "parallel", StepsMetaType::getInstance,
            "out", ParallelOutParamsMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance
    );

    protected ParallelStepMetaType() {
        super("Parallel", "parallel", Set.of("parallel"));
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
