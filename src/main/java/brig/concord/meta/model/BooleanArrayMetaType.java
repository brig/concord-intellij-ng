package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlArrayType;

public class BooleanArrayMetaType extends YamlArrayType {

    private static final BooleanArrayMetaType INSTANCE = new BooleanArrayMetaType();

    public static BooleanArrayMetaType getInstance() {
        return INSTANCE;
    }

    public BooleanArrayMetaType() {
        super(BooleanMetaType.getInstance());
    }
}
