package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.meta.model.YamlStringType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class ThrowStepMetaType extends IdentityMetaType {

    private static final ThrowStepMetaType INSTANCE = new ThrowStepMetaType();

    public static ThrowStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "throw", new YamlStringType("string", desc("doc.step.throw.key.description").andRequired()),
            "name", StepNameMetaType.getInstance());

    private ThrowStepMetaType() {
        super("throw", desc("doc.step.throw.description"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
