package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;

import java.util.List;

public class ExprOutParamsMetaType extends YamlAnyOfType {

    private static final ExprOutParamsMetaType INSTANCE = new ExprOutParamsMetaType();

    public static ExprOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected ExprOutParamsMetaType() {
        super("out params [object|string]", List.of(StringMetaType.getInstance(), AnyMapMetaType.getInstance()));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
