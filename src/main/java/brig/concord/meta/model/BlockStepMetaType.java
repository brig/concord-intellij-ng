package brig.concord.meta.model;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class BlockStepMetaType extends GroupOfStepsMetaType {

    private static final BlockStepMetaType INSTANCE = new BlockStepMetaType();

    public static BlockStepMetaType getInstance() {
        return INSTANCE;
    }

    private BlockStepMetaType() {
        super("block", "doc.step.block.key.description", desc("doc.step.block.description"));
    }
}
