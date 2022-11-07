package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlArrayType;

public class StepsMetaType extends YamlArrayType {

    private static final StepsMetaType INSTANCE = new StepsMetaType();

    public static StepsMetaType getInstance() {
        return INSTANCE;
    }

    public StepsMetaType() {
        super(StepElementMetaType.getInstance());
    }
}
