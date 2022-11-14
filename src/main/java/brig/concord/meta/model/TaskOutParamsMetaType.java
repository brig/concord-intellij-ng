package brig.concord.meta.model;

import brig.concord.meta.ConcordAnyMapMetaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.List;

public class TaskOutParamsMetaType extends YamlAnyOfType {

    private static final TaskOutParamsMetaType INSTANCE = new TaskOutParamsMetaType();

    public static TaskOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected TaskOutParamsMetaType() {
        super("out params [object|expression]", List.of(YamlStringType.getInstance(), ConcordAnyMapMetaType.getInstance()));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return ConcordAnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
