package brig.concord.meta.model;

import brig.concord.meta.ConcordStringArrayMetaType;
import org.jetbrains.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.List;

public class GroupOfStepsOutParamsMetaType extends YamlAnyOfType {

    private static final GroupOfStepsOutParamsMetaType INSTANCE = new GroupOfStepsOutParamsMetaType();

    public static GroupOfStepsOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected GroupOfStepsOutParamsMetaType() {
        super("out params [array|string]", List.of(YamlStringType.getInstance(), ConcordStringArrayMetaType.getInstance()));
    }
}
