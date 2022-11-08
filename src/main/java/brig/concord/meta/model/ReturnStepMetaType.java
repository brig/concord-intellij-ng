package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ReturnStepMetaType extends StepMetaType {

    private static final ReturnStepMetaType INSTANCE = new ReturnStepMetaType();

    public static ReturnStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "return", YamlStringType::getInstance);

    protected ReturnStepMetaType() {
        super("Return", "return", Set.of("return"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
