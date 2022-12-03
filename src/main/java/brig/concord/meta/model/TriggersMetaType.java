package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlArrayType;

@SuppressWarnings("UnstableApiUsage")
public class TriggersMetaType extends YamlArrayType {

    private static final TriggersMetaType INSTANCE = new TriggersMetaType();

    public static TriggersMetaType getInstance() {
        return INSTANCE;
    }

    public TriggersMetaType() {
        super(TriggerElementMetaType.getInstance());
    }
}
