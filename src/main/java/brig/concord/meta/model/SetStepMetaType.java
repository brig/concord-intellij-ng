package brig.concord.meta.model;

import brig.concord.meta.ConcordAnyMapMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class SetStepMetaType extends StepMetaType {

    private static final SetStepMetaType INSTANCE = new SetStepMetaType();

    public static SetStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "set", ConcordAnyMapMetaType::getInstance);

    protected SetStepMetaType() {
        super("Set", "set", Set.of("set"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
