package brig.concord.meta;

import brig.concord.meta.model.AnythingMetaType;
import org.jetbrains.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

public final class MetaUtils {

    public static boolean isAnything(YamlMetaType type) {
        if (type instanceof AnythingMetaType) {
            return true;
        }

        if (type instanceof ConcordMetaTypeProxy.YamlMetaTypeProxy proxy) {
            return isAnything(proxy.original());
        }

        return false;
    }

    public static boolean isAnyOf(YamlMetaType type) {
        if (type instanceof YamlAnyOfType) {
            return true;
        }

        if (type instanceof ConcordMetaTypeProxy.YamlMetaTypeProxy proxy) {
            return isAnyOf(proxy.original());
        }

        return false;
    }

    private MetaUtils() {
    }
}
