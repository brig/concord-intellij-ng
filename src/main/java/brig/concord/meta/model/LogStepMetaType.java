package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class LogStepMetaType extends IdentityMetaType {

    private static final LogStepMetaType INSTANCE = new LogStepMetaType();

    public static LogStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = StepFeatures.combine(
            StepFeatures.nameAndMeta(),
            Map.of("log", new StringMetaType(descKey("doc.step.log.key.description").andRequired()))
    );

    private LogStepMetaType() {
        super("log", descKey("doc.step.log.description"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }
}
