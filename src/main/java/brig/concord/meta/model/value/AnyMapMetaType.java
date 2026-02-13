package brig.concord.meta.model.value;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlMetaType;

public class AnyMapMetaType extends MapMetaType {

    private static final AnyMapMetaType INSTANCE = new AnyMapMetaType();

    public static AnyMapMetaType getInstance() {
        return INSTANCE;
    }

    public AnyMapMetaType() {
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnythingMetaType.getInstance().findFeatureByName(name);
    }

    @Override
    protected YamlMetaType getMapEntryType(String name) {
        return AnythingMetaType.getInstance();
    }
}
