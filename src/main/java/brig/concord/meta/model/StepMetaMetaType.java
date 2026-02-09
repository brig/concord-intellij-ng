package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;

public class StepMetaMetaType extends AnyMapMetaType {

    private static final StepMetaMetaType INSTANCE = new StepMetaMetaType();

    public static StepMetaMetaType getInstance() {
        return INSTANCE;
    }
}
