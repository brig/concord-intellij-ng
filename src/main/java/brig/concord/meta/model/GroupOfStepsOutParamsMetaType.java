package brig.concord.meta.model;

import brig.concord.meta.model.value.StringArrayMetaType;
import brig.concord.meta.model.value.StringMetaType;

import brig.concord.yaml.meta.model.YamlAnyOfType;

import java.util.List;

public class GroupOfStepsOutParamsMetaType extends YamlAnyOfType {

    private static final GroupOfStepsOutParamsMetaType INSTANCE = new GroupOfStepsOutParamsMetaType();

    public static GroupOfStepsOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected GroupOfStepsOutParamsMetaType() {
        super(StringMetaType.getInstance(), StringArrayMetaType.getInstance());

        setDescriptionKey("doc.step.feature.out.description");
    }
}
