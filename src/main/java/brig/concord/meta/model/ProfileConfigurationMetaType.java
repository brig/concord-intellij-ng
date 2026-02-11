package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.documentation.Documented;
import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.meta.model.value.*;

import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlIntegerType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ProfileConfigurationMetaType extends ConcordMetaType implements Documented, HighlightProvider {

    private static final ProfileConfigurationMetaType INSTANCE = new ProfileConfigurationMetaType();

    private static final Map<String, YamlMetaType> features = new HashMap<>();

    static {
        features.put("runtime", new YamlEnumType("runtime").withLiterals("concord-v2"));
        features.put("debug", BooleanMetaType.getInstance());
        features.put("entryPoint", CallMetaType.getInstance());
        features.put("dependencies", DependenciesMetaType.getInstance());
        features.put("extraDependencies", DependenciesMetaType.getInstance());
        features.put("arguments", AnyMapMetaType.getInstance());
        features.put("meta", AnyMapMetaType.getInstance());
        features.put("events", EventsMetaType.getInstance());
        features.put("requirements", AnyMapMetaType.getInstance());
        features.put("processTimeout", DurationMetaType.getInstance());
        features.put("suspendTimeout", DurationMetaType.getInstance());
        features.put("exclusive", ProcessExclusiveMetaType.getInstance());
        features.put("out", StringArrayMetaType.getInstance());
        features.put("template", StringMetaType.getInstance());
        features.put("parallelLoopParallelism", IntegerMetaType.getInstance());
    }

    public static ProfileConfigurationMetaType getInstance() {
        return INSTANCE;
    }

    private ProfileConfigurationMetaType() {
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public String getDescription() {
        return ConcordBundle.message("Configuration.description");
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }
}
