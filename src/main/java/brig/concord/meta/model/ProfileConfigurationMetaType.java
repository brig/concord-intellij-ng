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

public class ProfileConfigurationMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ProfileConfigurationMetaType INSTANCE = new ProfileConfigurationMetaType();

    private static final Map<String, YamlMetaType> features = new HashMap<>();

    static {
        features.put("runtime", new YamlEnumType("runtime").withLiterals("concord-v2").withDocBundlePrefix("doc.configuration.runtime"));
        features.put("debug", new BooleanMetaType().withDocBundlePrefix("doc.configuration.debug"));
        features.put("entryPoint", new CallMetaType().withDocBundlePrefix("doc.configuration.entryPoint"));
        features.put("dependencies", new DependenciesMetaType().withDocBundlePrefix("doc.configuration.dependencies"));
        features.put("extraDependencies", DependenciesMetaType.getInstance());
        features.put("arguments", new AnyMapMetaType().withDocBundlePrefix("doc.configuration.arguments"));
        features.put("meta", new AnyMapMetaType().withDocBundlePrefix("doc.configuration.meta"));
        features.put("events", EventsMetaType.getInstance());
        features.put("requirements", new AnyMapMetaType().withDocBundlePrefix("doc.configuration.requirements"));
        features.put("processTimeout", new DurationMetaType().withDocBundlePrefix("doc.configuration.processTimeout"));
        features.put("suspendTimeout", new DurationMetaType().withDocBundlePrefix("doc.configuration.suspendTimeout"));
        features.put("exclusive", ProcessExclusiveMetaType.getInstance());
        features.put("out", new StringArrayMetaType().withDocBundlePrefix("doc.configuration.out"));
        features.put("template", new StringMetaType().withDocBundlePrefix("doc.configuration.template"));
        features.put("parallelLoopParallelism", new IntegerMetaType().withDocBundlePrefix("doc.configuration.parallelLoopParallelism"));
    }

    public static ProfileConfigurationMetaType getInstance() {
        return INSTANCE;
    }

    private ProfileConfigurationMetaType() {
        setDescriptionKey("doc.profile.configuration.description");
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
