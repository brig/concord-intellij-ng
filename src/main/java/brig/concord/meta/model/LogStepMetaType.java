package brig.concord.meta.model;

import brig.concord.meta.model.value.StringMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public class LogStepMetaType extends IdentityMetaType {

    private static final LogStepMetaType INSTANCE = new LogStepMetaType();

    public static LogStepMetaType getInstance() {
        return INSTANCE;
    }

    // lazy init via holder to break circular static dependency through StepsMetaType
    private static class FeaturesHolder {
        static final Map<String, YamlMetaType> FEATURES = StepFeatures.combine(
                StepFeatures.NAME_AND_META,
                Map.of("log", StringMetaType.getInstance())
        );
    }

    protected LogStepMetaType() {
        super("Log", "log", Set.of("log"));
    }

    @Override
    public @NotNull Map<String, YamlMetaType> getFeatures() {
        return FeaturesHolder.FEATURES;
    }
}
