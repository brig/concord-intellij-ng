package brig.concord.meta.model;

public class BlockStepMetaType extends GroupOfStepsMetaType {

    private static final BlockStepMetaType INSTANCE = new BlockStepMetaType();

    public static BlockStepMetaType getInstance() {
        return INSTANCE;
    }

    protected BlockStepMetaType() {
        super("block");
    }
}
