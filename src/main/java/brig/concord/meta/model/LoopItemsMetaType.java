package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.ExpressionMetaType;

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

    private LoopItemsMetaType() {
        super(ExpressionMetaType.getInstance(), new YamlArrayType(LoopArrayItemMetaType.getInstance()), AnyMapMetaType.getInstance());

        setDescriptionKey("doc.step.feature.loop.items.description");
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
