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
        features.put("recordEvents", doc(new BooleanMetaType(), "doc.configuration.events.recordEvents"));
        features.put("recordTaskInVars", doc(new BooleanMetaType(), "doc.configuration.events.recordTaskInVars"));
        features.put("truncateMaxStringLength", doc(new IntegerMetaType(), "doc.configuration.events.truncateMaxStringLength"));
        features.put("truncateMaxArrayLength", doc(new IntegerMetaType(), "doc.configuration.events.truncateMaxArrayLength"));
        features.put("truncateMaxDepth", doc(new IntegerMetaType(), "doc.configuration.events.truncateMaxDepth"));
        features.put("recordTaskOutVars", doc(new BooleanMetaType(), "doc.configuration.events.recordTaskOutVars"));
        features.put("truncateInVars", doc(new BooleanMetaType(), "doc.configuration.events.truncateInVars"));
        features.put("truncateOutVars", doc(new BooleanMetaType(), "doc.configuration.events.truncateOutVars"));
        features.put("inVarsBlacklist", doc(new StringArrayMetaType(), "doc.configuration.events.inVarsBlacklist"));
        features.put("outVarsBlacklist", doc(new StringArrayMetaType(), "doc.configuration.events.outVarsBlacklist"));
        features.put("recordTaskMeta", doc(new BooleanMetaType(), "doc.configuration.events.recordTaskMeta"));
        features.put("truncateMeta", doc(new BooleanMetaType(), "doc.configuration.events.truncateMeta"));
        features.put("metaBlacklist", doc(new StringArrayMetaType(), "doc.configuration.events.metaBlacklist"));
    }

    private EventsMetaType() {
        setDocBundlePrefix("doc.configuration.events");
    }

    public static EventsMetaType getInstance() {
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
