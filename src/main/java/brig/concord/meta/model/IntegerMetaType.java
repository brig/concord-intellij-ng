package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlIntegerType;

public class IntegerMetaType extends YamlIntegerType {

    private static final IntegerMetaType INSTANCE = new IntegerMetaType();

    public static IntegerMetaType getInstance() {
        return INSTANCE;
    }

    public IntegerMetaType() {
        super(false);
    }
}
