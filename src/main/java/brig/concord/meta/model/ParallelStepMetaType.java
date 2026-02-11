package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class ParallelStepMetaType extends IdentityMetaType {

    private static final ParallelStepMetaType INSTANCE = new ParallelStepMetaType();

    public static ParallelStepMetaType getInstance() {
        return INSTANCE;
    }

    // lazy init via holder to break circular static dependency through StepsMetaType
    private static class FeaturesHolder {
        static final Map<String, YamlMetaType> FEATURES = Map.of(
                "parallel", StepsMetaType.getInstance(),
                "out", ParallelOutParamsMetaType.getInstance(),
                "meta", StepMetaMetaType.getInstance()
        );
    }

    protected ParallelStepMetaType() {
        super("Parallel", "parallel", Set.of("parallel"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return FeaturesHolder.FEATURES;
    }
}
