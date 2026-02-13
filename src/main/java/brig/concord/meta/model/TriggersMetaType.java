package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlArrayType;

public class TriggersMetaType extends YamlArrayType {

    private static final TriggersMetaType INSTANCE = new TriggersMetaType();

    public static TriggersMetaType getInstance() {
        return INSTANCE;
    }

    public TriggersMetaType() {
        super(TriggerElementMetaType.getInstance());
        setDescriptionKey("doc.triggers.description");
    }
}
