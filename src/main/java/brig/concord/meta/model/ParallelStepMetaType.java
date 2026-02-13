package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class ParallelStepMetaType extends IdentityMetaType {

    private static final ParallelStepMetaType INSTANCE = new ParallelStepMetaType();

    public static ParallelStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features;

    static {
        var parallelSteps = new StepsMetaType(desc("doc.step.parallel.key.description").andRequired());
        features = Map.of(
                "parallel", parallelSteps,
                "out", ParallelOutParamsMetaType.getInstance(),
                "meta", StepMetaMetaType.getInstance()
        );
    }

    private ParallelStepMetaType() {
        super("parallel", desc("doc.step.parallel.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
