package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class StepFeatures {

    public static final Map<String, Supplier<YamlMetaType>> NAME_AND_META = Map.of(
            "name", StepNameMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance
    );

    public static final Map<String, Supplier<YamlMetaType>> ERROR = Map.of(
            "error", StepsMetaType::getInstance
    );

    public static final Map<String, Supplier<YamlMetaType>> LOOP_AND_RETRY = Map.of(
            "loop", LoopMetaType::getInstance,
            "retry", RetryMetaType::getInstance
    );

    @SafeVarargs
    public static Map<String, Supplier<YamlMetaType>> combine(Map<String, Supplier<YamlMetaType>>... maps) {
        var result = new LinkedHashMap<String, Supplier<YamlMetaType>>();
        for (var map : maps) {
            result.putAll(map);
        }
        return Map.copyOf(result);
    }

    private StepFeatures() {
    }
}
