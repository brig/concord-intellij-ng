package brig.concord.meta.model.call;

import brig.concord.meta.model.AnythingMetaType;

public class AnyInParamMetaType extends AnythingMetaType implements CallInParamMetaType {

    private static final AnyInParamMetaType INSTANCE = new AnyInParamMetaType();

    public static AnyInParamMetaType getInstance() {
        return INSTANCE;
    }
}
