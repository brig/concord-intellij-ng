package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

public class StepsMetaType extends YamlArrayType {

    private static final StepsMetaType INSTANCE = new StepsMetaType();

    public static StepsMetaType getInstance() {
        return INSTANCE;
    }

    public StepsMetaType() {
        super("step[]");
    }

    @Override
    public @NotNull YamlMetaType getElementType() {
        return StepElementMetaType.getInstance();
    }
}
