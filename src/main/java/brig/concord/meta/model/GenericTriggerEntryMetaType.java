package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.IntegerMetaType;
import brig.concord.meta.model.value.StringArrayMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.desc;
import static brig.concord.yaml.meta.model.TypeProps.required;

public class GenericTriggerEntryMetaType extends ConcordMetaType {

    private static final GenericTriggerEntryMetaType INSTANCE = new GenericTriggerEntryMetaType();

    public static GenericTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "entryPoint", new CallMetaType(desc("doc.step.call.key.description").andRequired()),
            "activeProfiles", StringArrayMetaType.getInstance(),
            "arguments", AnyMapMetaType.getInstance(),
            "exclusive", TriggerExclusiveMetaType.getInstance(),
            "conditions", new AnyMapMetaType(required()),
            "version", new IntegerMetaType(required())
    );

    private GenericTriggerEntryMetaType() {
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

}
