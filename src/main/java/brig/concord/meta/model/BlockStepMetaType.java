package brig.concord.meta.model;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class BlockStepMetaType extends GroupOfStepsMetaType {

    private static final BlockStepMetaType INSTANCE = new BlockStepMetaType();

    public static BlockStepMetaType getInstance() {
        return INSTANCE;
    }

    private BlockStepMetaType() {
        super("block", "doc.step.block.key.description", descKey("doc.step.block.description"));
    }
}
