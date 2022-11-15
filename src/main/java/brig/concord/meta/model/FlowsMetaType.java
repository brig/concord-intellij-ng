package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlMetaType;

public class FlowsMetaType extends MapMetaType {

    private static final FlowsMetaType INSTANCE = new FlowsMetaType();

    public static FlowsMetaType getInstance() {
        return INSTANCE;
    }

    protected FlowsMetaType() {
        super("FLows");
    }

    @Override
    protected YamlMetaType getMapEntryType(String name) {
        return StepsMetaType.getInstance();
    }
}
