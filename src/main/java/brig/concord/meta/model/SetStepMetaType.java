package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class SetStepMetaType extends IdentityMetaType {

    private static final SetStepMetaType INSTANCE = new SetStepMetaType();

    public static SetStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "set", new AnyMapMetaType().withDescriptionKey("doc.step.set.key.description"));

    protected SetStepMetaType() {
        super("set", Set.of("set"));

        setDescriptionKey("doc.step.set.description");
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
