package brig.concord.meta.model.call;

import brig.concord.meta.model.AnyMapMetaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlMetaType;

@SuppressWarnings("UnstableApiUsage")
public class AnyMapInParamMetaType extends AnyMapMetaType implements CallInParamMetaType {

    private static final AnyMapInParamMetaType INSTANCE = new AnyMapInParamMetaType();

    public static AnyMapInParamMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyInParamMetaType.getInstance().findFeatureByName(name);
    }

    @Override
    protected YamlMetaType getMapEntryType(String name) {
        return AnyInParamMetaType.getInstance();
    }
}
