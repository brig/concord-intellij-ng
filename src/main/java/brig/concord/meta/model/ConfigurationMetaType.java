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
        features.put("runtime", new YamlEnumType("string").withLiterals("concord-v2").withDescriptionKey("doc.configuration.runtime.description"));
        features.put("debug", new BooleanMetaType().withDescriptionKey("doc.configuration.debug.description"));
        features.put("entryPoint", new CallMetaType().withDescriptionKey("doc.configuration.entryPoint.description"));
        features.put("dependencies", new DependenciesMetaType().withDescriptionKey("doc.configuration.dependencies.description"));
        features.put("arguments", new AnyMapMetaType().withDescriptionKey("doc.configuration.arguments.description"));
        features.put("meta", new AnyMapMetaType().withDescriptionKey("doc.configuration.meta.description"));
        features.put("events", EventsMetaType.getInstance());
        features.put("requirements", new AnyMapMetaType().withDescriptionKey("doc.configuration.requirements.description"));
        features.put("processTimeout", new DurationMetaType().withDescriptionKey("doc.configuration.processTimeout.description"));
        features.put("suspendTimeout", new DurationMetaType().withDescriptionKey("doc.configuration.suspendTimeout.description"));
        features.put("exclusive", ProcessExclusiveMetaType.getInstance());
        features.put("out", new StringArrayMetaType().withDescriptionKey("doc.configuration.out.description"));
        features.put("template", new StringMetaType().withDescriptionKey("doc.configuration.template.description"));
        features.put("parallelLoopParallelism", new IntegerMetaType().withDescriptionKey("doc.configuration.parallelLoopParallelism.description"));
    }

    private ConfigurationMetaType() {
        setDescriptionKey("doc.configuration.description");
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
}
