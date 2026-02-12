package brig.concord.meta.model;

public class TryStepMetaType extends GroupOfStepsMetaType {

    private static final TryStepMetaType INSTANCE = new TryStepMetaType();

    public static TryStepMetaType getInstance() {
        return INSTANCE;
    }

    protected TryStepMetaType() {
        super("try");

        setDescriptionKey("doc.step.try.description");
    }
}
