package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.List;

public class ParallelOutParamsMetaType extends YamlAnyOfType {

    private static final ParallelOutParamsMetaType INSTANCE = new ParallelOutParamsMetaType();

    public static ParallelOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected ParallelOutParamsMetaType() {
        super("out params [object|array|string]", List.of(YamlStringType.getInstance(), AnyMapMetaType.getInstance(), StringArrayMetaType.getInstance()));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
