package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class SetStepMetaType extends IdentityMetaType {

    private static final SetStepMetaType INSTANCE = new SetStepMetaType();

    public static SetStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "set", AnyMapMetaType::getInstance);

    protected SetStepMetaType() {
        super("Set", "set", Set.of("set"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
