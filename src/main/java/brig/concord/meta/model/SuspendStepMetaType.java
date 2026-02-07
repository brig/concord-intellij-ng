package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SuspendStepMetaType extends IdentityMetaType {

    private static final SuspendStepMetaType INSTANCE = new SuspendStepMetaType();

    public static SuspendStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "suspend", StringMetaType::getInstance);

    protected SuspendStepMetaType() {
        super("Suspend", "suspend", Set.of("suspend"));
    }

    @Override
    public @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
