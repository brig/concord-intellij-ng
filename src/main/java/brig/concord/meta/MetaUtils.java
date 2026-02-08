package brig.concord.meta;

import brig.concord.meta.model.value.AnythingMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;

public final class MetaUtils {

    public static boolean isAnything(YamlMetaType type) {
        return type instanceof AnythingMetaType;
    }

    private MetaUtils() {
    }
}
