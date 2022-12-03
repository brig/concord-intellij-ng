package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class GenericTriggerEntryMetaType extends ConcordMetaType {

    private static final GenericTriggerEntryMetaType INSTANCE = new GenericTriggerEntryMetaType();

    public static GenericTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("entryPoint", "conditions", "version");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "entryPoint", StringMetaType::getInstance,
            "activeProfiles", StringArrayMetaType::getInstance,
            "arguments", AnyMapMetaType::getInstance,
            "exclusive", TriggerExclusiveMetaType::getInstance,
            "conditions", AnyMapMetaType::getInstance,
            "version", IntegerMetaType::getInstance
    );

    protected GenericTriggerEntryMetaType() {
        super("generic trigger entry");
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
