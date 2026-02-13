package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class StepMetaMetaType extends AnyMapMetaType {

    private static final StepMetaMetaType INSTANCE = new StepMetaMetaType();

    public static StepMetaMetaType getInstance() {
        return INSTANCE;
    }

    private StepMetaMetaType() {
        super(desc("doc.step.feature.meta.description"));
    }
}
