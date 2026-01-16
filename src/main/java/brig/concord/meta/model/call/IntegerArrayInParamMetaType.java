package brig.concord.meta.model.call;

import brig.concord.meta.model.IntegerArrayMetaType;

public class IntegerArrayInParamMetaType extends IntegerArrayMetaType implements CallInParamMetaType {

    private static final IntegerArrayInParamMetaType INSTANCE = new IntegerArrayInParamMetaType();

    public static IntegerArrayInParamMetaType getInstance() {
        return INSTANCE;
    }
}
