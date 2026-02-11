package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.BooleanMetaType;
import brig.concord.meta.model.value.IntegerMetaType;
import brig.concord.meta.model.value.StringArrayMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EventsMetaType extends ConcordMetaType implements HighlightProvider {

    private static final EventsMetaType INSTANCE = new EventsMetaType();

    private static final Map<String, YamlMetaType> features = new HashMap<>();

    static {
        features.put("recordEvents", BooleanMetaType.getInstance());
        features.put("recordTaskInVars", BooleanMetaType.getInstance());
        features.put("truncateMaxStringLength", IntegerMetaType.getInstance());
        features.put("truncateMaxArrayLength", IntegerMetaType.getInstance());
        features.put("truncateMaxDepth", IntegerMetaType.getInstance());
        features.put("recordTaskOutVars", BooleanMetaType.getInstance());
        features.put("truncateInVars", BooleanMetaType.getInstance());
        features.put("truncateOutVars", BooleanMetaType.getInstance());
        features.put("inVarsBlacklist", StringArrayMetaType.getInstance());
        features.put("outVarsBlacklist", StringArrayMetaType.getInstance());
        features.put("recordTaskMeta", BooleanMetaType.getInstance());
        features.put("truncateMeta", BooleanMetaType.getInstance());
        features.put("metaBlacklist", StringArrayMetaType.getInstance());
    }

    public static EventsMetaType getInstance() {
        return INSTANCE;
    }

    protected EventsMetaType() {
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
