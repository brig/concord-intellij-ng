package brig.concord.meta.model;

import brig.concord.meta.model.value.StringArrayMetaType;
import brig.concord.meta.model.value.StringMetaType;
import brig.concord.yaml.meta.model.YamlAnyOfType;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class GroupOfStepsOutParamsMetaType extends YamlAnyOfType {

    private static final GroupOfStepsOutParamsMetaType INSTANCE = new GroupOfStepsOutParamsMetaType();

    public static GroupOfStepsOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    private GroupOfStepsOutParamsMetaType() {
        super(List.of(StringMetaType.getInstance(), StringArrayMetaType.getInstance()),
                desc("doc.step.feature.out.description"));
    }
}
