package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlArrayType;

import java.util.List;

public class LoopItemsMetaType extends YamlAnyOfType {

    private static final LoopItemsMetaType INSTANCE = new LoopItemsMetaType();

    public static LoopItemsMetaType getInstance() {
        return INSTANCE;
    }

    protected LoopItemsMetaType() {
        super("expression|array|object", List.of(ExpressionMetaType.getInstance(), new YamlArrayType(LoopArrayItemMetaType.getInstance()), AnyMapMetaType.getInstance()));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
