package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class SuspendStepMetaType extends IdentityMetaType {

    private static final SuspendStepMetaType INSTANCE = new SuspendStepMetaType();

    public static SuspendStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "suspend", new StringMetaType(desc("doc.step.suspend.key.description").andRequired()));

    private SuspendStepMetaType() {
        super("suspend", desc("doc.step.suspend.description"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
