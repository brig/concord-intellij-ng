package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlArrayType;

public class IntegerArrayMetaType extends YamlArrayType {

    private static final IntegerArrayMetaType INSTANCE = new IntegerArrayMetaType();

    public static IntegerArrayMetaType getInstance() {
        return INSTANCE;
    }

    public IntegerArrayMetaType() {
        super(IntegerMetaType.getInstance());
    }
}
