package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class SuspendStepMetaType extends IdentityMetaType {

    private static final SuspendStepMetaType INSTANCE = new SuspendStepMetaType();

    public static SuspendStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "suspend", new StringMetaType().withDescriptionKey("doc.step.suspend.key.description"));

    private SuspendStepMetaType() {
        super("suspend", Set.of("suspend"));

        setDescriptionKey("doc.step.suspend.description");
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
