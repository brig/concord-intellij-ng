package brig.concord.meta.model;

import brig.concord.meta.ConcordAnyMapMetaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlAnyOfType;

import java.util.List;

public class OutParamsMetaType extends YamlAnyOfType {

    private static final OutParamsMetaType INSTANCE = new OutParamsMetaType();

    public static OutParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected OutParamsMetaType() {
        super("out params [object|expression]", List.of(ExpressionMetaType.getInstance(), ConcordAnyMapMetaType.getInstance()));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return ConcordAnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
