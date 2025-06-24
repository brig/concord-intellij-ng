package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.meta.model.YamlStringType;

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
            "name", StringMetaType::getInstance);

    protected ThrowStepMetaType() {
        super("Throw", "throw", Set.of("throw"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
