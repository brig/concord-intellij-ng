package brig.concord.meta.model;

import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlArrayType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjectArrayMetaType extends YamlArrayType {

    private static final ObjectArrayMetaType INSTANCE = new ObjectArrayMetaType();

    public static ObjectArrayMetaType getInstance() {
        return INSTANCE;
    }

    public ObjectArrayMetaType() {
        super(AnyMapMetaType.getInstance());
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
