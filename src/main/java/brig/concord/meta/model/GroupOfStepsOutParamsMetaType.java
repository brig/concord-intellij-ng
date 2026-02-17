package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlAnyOfType;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class GroupOfStepsOutParamsMetaType extends YamlAnyOfType {

    private static final GroupOfStepsOutParamsMetaType INSTANCE = new GroupOfStepsOutParamsMetaType();

    public static GroupOfStepsOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    private GroupOfStepsOutParamsMetaType() {
        super(List.of(OutVarMetaType.getInstance(), OutVarArrayMetaType.getInstance()),
                descKey("doc.step.feature.out.description"));
    }
}
