package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StepFeatures {

    public static Map<String, YamlMetaType> nameAndMeta() {
        return Map.of(
                "name", StepNameMetaType.getInstance(),
                "meta", StepMetaMetaType.getInstance()
        );
    }

    public static Map<String, YamlMetaType> error() {
        return Map.of(
                "error", StepsMetaType.getInstance()
        );
    }

    public static Map<String, YamlMetaType> loopAndRetry() {
        return Map.of(
                "loop", LoopMetaType.getInstance(),
                "retry", RetryMetaType.getInstance()
        );
    }

    @SafeVarargs
    public static Map<String, YamlMetaType> combine(Map<String, YamlMetaType>... maps) {
        var result = new LinkedHashMap<String, YamlMetaType>();
        for (var map : maps) {
            result.putAll(map);
        }
        return Map.copyOf(result);
    }

    private StepFeatures() {
    }
}
