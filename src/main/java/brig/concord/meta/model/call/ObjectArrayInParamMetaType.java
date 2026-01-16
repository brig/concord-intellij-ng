package brig.concord.meta.model.call;

import brig.concord.meta.model.ObjectArrayMetaType;
import brig.concord.yaml.meta.model.Field;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ObjectArrayInParamMetaType extends ObjectArrayMetaType implements CallInParamMetaType {

    private static final ObjectArrayInParamMetaType INSTANCE = new ObjectArrayInParamMetaType();

    public static ObjectArrayInParamMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return super.findFeatureByName(name);
    }
}
