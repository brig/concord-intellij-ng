package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CheckpointStepMetaType extends IdentityMetaType {

    private static final CheckpointStepMetaType INSTANCE = new CheckpointStepMetaType();

    public static CheckpointStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "checkpoint", StringMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance);

    protected CheckpointStepMetaType() {
        super("Checkpoint", "checkpoint", Set.of("checkpoint"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
