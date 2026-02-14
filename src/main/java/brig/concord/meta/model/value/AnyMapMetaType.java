package brig.concord.meta.model.value;

import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnyMapMetaType extends MapMetaType {

    private static final AnyMapMetaType INSTANCE = new AnyMapMetaType();

    public static AnyMapMetaType getInstance() {
        return INSTANCE;
    }

    public AnyMapMetaType() {
    }

    public AnyMapMetaType(@NotNull TypeProps props) {
        super(props);
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
