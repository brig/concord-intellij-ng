package brig.concord.meta.model.call;

import brig.concord.meta.model.StringArrayMetaType;

public class StringArrayInParamMetaType extends StringArrayMetaType implements CallInParamMetaType {

    private static final StringArrayInParamMetaType INSTANCE = new StringArrayInParamMetaType();

    public static StringArrayInParamMetaType getInstance() {
        return INSTANCE;
    }
}
