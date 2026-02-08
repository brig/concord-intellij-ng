package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class GenericTriggerEntryMetaType extends ConcordMetaType {

    private static final GenericTriggerEntryMetaType INSTANCE = new GenericTriggerEntryMetaType();

    public static GenericTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("entryPoint", "conditions", "version");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "entryPoint", CallMetaType::getInstance,
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
    protected @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return required;
    }
}
