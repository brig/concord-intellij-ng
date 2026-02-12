package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.meta.model.YamlStringType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class ThrowStepMetaType extends IdentityMetaType {

    private static final ThrowStepMetaType INSTANCE = new ThrowStepMetaType();

    public static ThrowStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "throw", new YamlStringType().withDescriptionKey("doc.step.throw.key.description"),
            "name", StepNameMetaType.getInstance());

    protected ThrowStepMetaType() {
        super("throw", Set.of("throw"));

        setDescriptionKey("doc.step.throw.description");
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
