package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlAnyOfType;

import java.util.List;

public class InParamsMetaType extends YamlAnyOfType {

    private static final InParamsMetaType INSTANCE = new InParamsMetaType();

    public static InParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected InParamsMetaType() {
        super("in params [object|expression]", List.of(ExpressionMetaType.getInstance(), AnyMapMetaType.getInstance()));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
