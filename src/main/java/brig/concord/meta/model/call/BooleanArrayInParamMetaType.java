package brig.concord.meta.model.call;

import brig.concord.meta.model.BooleanArrayMetaType;

public class BooleanArrayInParamMetaType extends BooleanArrayMetaType implements CallInParamMetaType {

    private static final BooleanArrayInParamMetaType INSTANCE = new BooleanArrayInParamMetaType();

    public static BooleanArrayInParamMetaType getInstance() {
        return INSTANCE;
    }
}
