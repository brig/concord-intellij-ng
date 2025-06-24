package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlAnyOfType;

import java.util.List;

public class GroupOfStepsOutParamsMetaType extends YamlAnyOfType {

    private static final GroupOfStepsOutParamsMetaType INSTANCE = new GroupOfStepsOutParamsMetaType();

    public static GroupOfStepsOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected GroupOfStepsOutParamsMetaType() {
        super("out params [array|string]", List.of(StringMetaType.getInstance(), StringArrayMetaType.getInstance()));
    }
}
