package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.meta.model.value.*;

import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ConfigurationMetaType INSTANCE = new ConfigurationMetaType();

    private static final Map<String, YamlMetaType> features = new HashMap<>();

    static {
        features.put("runtime", doc(new YamlEnumType("string").withLiterals("concord-v2"), "doc.configuration.runtime"));
        features.put("debug", doc(new BooleanMetaType(), "doc.configuration.debug"));
        features.put("entryPoint", doc(new CallMetaType(), "doc.configuration.entryPoint"));
        features.put("dependencies", doc(new DependenciesMetaType(), "doc.configuration.dependencies"));
        features.put("arguments", doc(new AnyMapMetaType(), "doc.configuration.arguments"));
        features.put("meta", doc(new AnyMapMetaType(), "doc.configuration.meta"));
        features.put("events", EventsMetaType.getInstance());
        features.put("requirements", doc(new AnyMapMetaType(), "doc.configuration.requirements"));
        features.put("processTimeout", doc(new DurationMetaType(), "doc.configuration.processTimeout"));
        features.put("suspendTimeout", doc(new DurationMetaType(), "doc.configuration.suspendTimeout"));
        features.put("exclusive", ProcessExclusiveMetaType.getInstance());
        features.put("out", doc(new StringArrayMetaType(), "doc.configuration.out"));
        features.put("template", doc(new StringMetaType(), "doc.configuration.template"));
        features.put("parallelLoopParallelism", doc(new IntegerMetaType(), "doc.configuration.parallelLoopParallelism"));
    }

    private ConfigurationMetaType() {
        setDocBundlePrefix("doc.configuration");
    }

    public static ConfigurationMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    private static <T extends YamlMetaType> T doc(T type, String prefix) {
        type.setDocBundlePrefix(prefix);
        return type;
    }
}
