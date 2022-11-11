package brig.concord.meta.model;

import brig.concord.meta.ConcordAnyMapMetaType;

public class FormsMetaType extends ConcordAnyMapMetaType {

    private static final FormsMetaType INSTANCE = new FormsMetaType();

    public static FormsMetaType getInstance() {
        return INSTANCE;
    }
}
