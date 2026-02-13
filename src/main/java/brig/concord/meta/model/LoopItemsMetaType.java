package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlArrayType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class LoopItemsMetaType extends YamlAnyOfType {

    private static final LoopItemsMetaType INSTANCE = new LoopItemsMetaType();

    public static LoopItemsMetaType getInstance() {
        return INSTANCE;
    }

    private LoopItemsMetaType() {
        super(List.of(ExpressionMetaType.getInstance(), new YamlArrayType(LoopArrayItemMetaType.getInstance()), AnyMapMetaType.getInstance()),
                desc("doc.step.feature.loop.items.description").andRequired());
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
