package brig.concord.meta.model;

public class StepMetaMetaType extends AnyMapMetaType {

    private static final StepMetaMetaType INSTANCE = new StepMetaMetaType();

    public static StepMetaMetaType getInstance() {
        return INSTANCE;
    }
}
