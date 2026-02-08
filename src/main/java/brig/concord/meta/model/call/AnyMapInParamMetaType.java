package brig.concord.meta.model.call;

import brig.concord.meta.model.AnyMapMetaType;

public class AnyMapInParamMetaType extends AnyMapMetaType implements CallInParamMetaType {

    private static final AnyMapInParamMetaType INSTANCE = new AnyMapInParamMetaType();

    public static AnyMapInParamMetaType getInstance() {
        return INSTANCE;
    }
}
