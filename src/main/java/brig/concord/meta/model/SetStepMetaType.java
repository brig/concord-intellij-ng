package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class SetStepMetaType extends IdentityMetaType {

    private static final SetStepMetaType INSTANCE = new SetStepMetaType();

    public static SetStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "set", new AnyMapMetaType(descKey("doc.step.set.key.description").andRequired()));

    private SetStepMetaType() {
        super("set", descKey("doc.step.set.description"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
