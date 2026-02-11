package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.LinkedHashMap;
import java.util.Map;

public final class StepFeatures {

    public static final Map<String, YamlMetaType> NAME_AND_META = Map.of(
            "name", StepNameMetaType.getInstance(),
            "meta", StepMetaMetaType.getInstance()
    );

    public static final Map<String, YamlMetaType> ERROR = Map.of(
            "error", StepsMetaType.getInstance()
    );

    public static final Map<String, YamlMetaType> LOOP_AND_RETRY = Map.of(
            "loop", LoopMetaType.getInstance(),
            "retry", RetryMetaType.getInstance()
    );

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
