package brig.concord.meta;

import org.jetbrains.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.yaml.meta.model.YamlAnything;
import org.jetbrains.yaml.meta.model.YamlMetaType;

public final class MetaUtils {

    public static boolean isAnything(YamlMetaType type) {
        if (type instanceof YamlAnything) {
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
