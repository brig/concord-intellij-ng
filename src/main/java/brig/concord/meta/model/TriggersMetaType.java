package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlArrayType;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class TriggersMetaType extends YamlArrayType {

    private static final TriggersMetaType INSTANCE = new TriggersMetaType();

    public static TriggersMetaType getInstance() {
        return INSTANCE;
    }

    public TriggersMetaType() {
        super(TriggerElementMetaType.getInstance(), descKey("doc.triggers.description"));
    }
}
