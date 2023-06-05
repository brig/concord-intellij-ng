package brig.concord.meta.model.call;

import brig.concord.meta.model.IntegerMetaType;

public class IntegerInParamMetaType extends IntegerMetaType implements CallInParamMetaType {

    private static final IntegerInParamMetaType INSTANCE = new IntegerInParamMetaType();

    public static IntegerInParamMetaType getInstance() {
        return INSTANCE;
    }
}
