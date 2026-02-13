package brig.concord.meta.model;

import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class StepsMetaType extends YamlArrayType {

    private static final StepsMetaType INSTANCE = new StepsMetaType();

    public static StepsMetaType getInstance() {
        return INSTANCE;
    }

    public StepsMetaType() {
        super("object[]|string[]", desc("doc.flows.flowName.description"));
    }

    public StepsMetaType(@NotNull TypeProps props) {
        super("object[]|string[]", props);
    }

    @Override
    public @NotNull YamlMetaType getElementType() {
        return StepElementMetaType.getInstance();
    }
}
