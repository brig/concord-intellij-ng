package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.meta.model.YamlStringType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ThrowStepMetaType extends IdentityMetaType {

    private static final ThrowStepMetaType INSTANCE = new ThrowStepMetaType();

    public static ThrowStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "throw", YamlStringType::getInstance,
            "name", StepNameMetaType::getInstance);

    protected ThrowStepMetaType() {
        super("Throw", "throw", Set.of("throw"));
    }

    @Override
    public @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
