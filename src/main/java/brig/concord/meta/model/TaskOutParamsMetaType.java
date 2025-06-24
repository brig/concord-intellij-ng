package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;

import java.util.List;

public class TaskOutParamsMetaType extends YamlAnyOfType {

    private static final TaskOutParamsMetaType INSTANCE = new TaskOutParamsMetaType();

    public static TaskOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected TaskOutParamsMetaType() {
        super("out params [object|expression]", List.of(StringMetaType.getInstance(), AnyMapMetaType.getInstance()));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
