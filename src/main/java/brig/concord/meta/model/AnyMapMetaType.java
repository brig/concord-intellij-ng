package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlMetaType;

@SuppressWarnings("UnstableApiUsage")
public class AnyMapMetaType extends MapMetaType {

    private static final AnyMapMetaType INSTANCE = new AnyMapMetaType();

    public static AnyMapMetaType getInstance() {
        return INSTANCE;
    }

    protected AnyMapMetaType() {
        super("Object");
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
