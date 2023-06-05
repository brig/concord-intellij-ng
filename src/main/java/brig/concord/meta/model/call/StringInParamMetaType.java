package brig.concord.meta.model.call;

import brig.concord.meta.model.StringMetaType;

public class StringInParamMetaType extends StringMetaType implements CallInParamMetaType {

    private static final StringInParamMetaType INSTANCE = new StringInParamMetaType();

    public static StringInParamMetaType getInstance() {
        return INSTANCE;
    }
}
