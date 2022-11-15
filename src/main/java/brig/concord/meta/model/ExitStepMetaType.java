package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ExitStepMetaType extends IdentityMetaType {

    private static final ExitStepMetaType INSTANCE = new ExitStepMetaType();

    public static ExitStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "exit", StringMetaType::getInstance);

    protected ExitStepMetaType() {
        super("Exit", "exit", Set.of("exit"));
    }

    @Override
    public Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
