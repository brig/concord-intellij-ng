package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.Map;

public class BlockStepMetaType extends GroupOfStepsMetaType {

    private static final BlockStepMetaType INSTANCE = new BlockStepMetaType();

    public static BlockStepMetaType getInstance() {
        return INSTANCE;
    }

    protected BlockStepMetaType() {
        super("block");
    }

    // lazy init via holder to break circular static dependency through StepsMetaType
    private static class FeaturesHolder {
        static final Map<String, YamlMetaType> FEATURES = StepFeatures.combine(
                StepFeatures.NAME_AND_META, StepFeatures.ERROR,
                Map.of("block", StepsMetaType.getInstance(),
                       "out", GroupOfStepsOutParamsMetaType.getInstance(),
                       "loop", LoopMetaType.getInstance())
        );
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return FeaturesHolder.FEATURES;
    }
}
