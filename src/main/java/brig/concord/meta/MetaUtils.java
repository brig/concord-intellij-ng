package brig.concord.meta;

import brig.concord.meta.model.AnythingMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

public final class MetaUtils {

    public static boolean isAnything(YamlMetaType type) {
        return type instanceof AnythingMetaType;
    }

    private MetaUtils() {
    }
}
