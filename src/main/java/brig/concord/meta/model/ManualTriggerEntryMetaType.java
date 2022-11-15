package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ManualTriggerEntryMetaType extends ConcordMetaType {

    private static final ManualTriggerEntryMetaType INSTANCE = new ManualTriggerEntryMetaType();

    public static ManualTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("entryPoint");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "name", YamlStringType::getInstance,
            "entryPoint", YamlStringType::getInstance,
            "activeProfiles", StringArrayMetaType::getInstance,
            "arguments", AnyMapMetaType::getInstance
            );

    protected ManualTriggerEntryMetaType() {
        super("manual trigger entry");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return required;
    }
}
