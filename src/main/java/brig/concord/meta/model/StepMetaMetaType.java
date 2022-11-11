package brig.concord.meta.model;

import brig.concord.meta.ConcordAnyMapMetaType;

public class StepMetaMetaType extends ConcordAnyMapMetaType {

    private static final StepMetaMetaType INSTANCE = new StepMetaMetaType();

    public static StepMetaMetaType getInstance() {
        return INSTANCE;
    }
}
