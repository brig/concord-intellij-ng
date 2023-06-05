package brig.concord.meta.model.call;

import brig.concord.meta.model.BooleanMetaType;

public class BooleanInParamMetaType extends BooleanMetaType implements CallInParamMetaType {

    private static final BooleanInParamMetaType INSTANCE = new BooleanInParamMetaType();

    public static BooleanInParamMetaType getInstance() {
        return INSTANCE;
    }
}
