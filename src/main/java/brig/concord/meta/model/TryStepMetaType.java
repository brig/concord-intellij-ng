package brig.concord.meta.model;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class TryStepMetaType extends GroupOfStepsMetaType {

    private static final TryStepMetaType INSTANCE = new TryStepMetaType();

    public static TryStepMetaType getInstance() {
        return INSTANCE;
    }

    private TryStepMetaType() {
        super("try", "doc.step.try.key.description", desc("doc.step.try.description"));
    }
}
