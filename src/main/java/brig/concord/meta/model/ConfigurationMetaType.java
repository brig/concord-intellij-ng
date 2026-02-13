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

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class ConfigurationMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ConfigurationMetaType INSTANCE = new ConfigurationMetaType();

    private static final Map<String, YamlMetaType> features = new HashMap<>();

    static {
        features.put("runtime", new YamlEnumType("string", desc("doc.configuration.runtime.description")).withLiterals("concord-v2"));
        features.put("debug", new BooleanMetaType(desc("doc.configuration.debug.description")));
        features.put("entryPoint", new CallMetaType(desc("doc.configuration.entryPoint.description")));
        features.put("dependencies", new DependenciesMetaType(desc("doc.configuration.dependencies.description")));
        features.put("arguments", new AnyMapMetaType(desc("doc.configuration.arguments.description")));
        features.put("meta", new AnyMapMetaType(desc("doc.configuration.meta.description")));
        features.put("events", EventsMetaType.getInstance());
        features.put("requirements", new AnyMapMetaType(desc("doc.configuration.requirements.description")));
        features.put("processTimeout", new DurationMetaType(desc("doc.configuration.processTimeout.description")));
        features.put("suspendTimeout", new DurationMetaType(desc("doc.configuration.suspendTimeout.description")));
        features.put("exclusive", ProcessExclusiveMetaType.getInstance());
        features.put("out", new StringArrayMetaType(desc("doc.configuration.out.description")));
        features.put("template", new StringMetaType(desc("doc.configuration.template.description")));
        features.put("parallelLoopParallelism", new IntegerMetaType(desc("doc.configuration.parallelLoopParallelism.description")));
    }

    private ConfigurationMetaType() {
        super(desc("doc.configuration.description"));
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
