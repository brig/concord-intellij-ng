package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class CheckpointStepMetaType extends IdentityMetaType {

    private static final CheckpointStepMetaType INSTANCE = new CheckpointStepMetaType();

    public static CheckpointStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "checkpoint", new StringMetaType(desc("doc.step.checkpoint.key.description").andRequired()),
            "meta", StepMetaMetaType.getInstance());

    private CheckpointStepMetaType() {
        super("checkpoint", desc("doc.step.checkpoint.description"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
