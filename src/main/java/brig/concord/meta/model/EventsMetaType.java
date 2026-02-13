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

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class EventsMetaType extends ConcordMetaType implements HighlightProvider {

    private static final EventsMetaType INSTANCE = new EventsMetaType();

    private static final Map<String, YamlMetaType> features = new HashMap<>();

    static {
        features.put("recordEvents", new BooleanMetaType(desc("doc.configuration.events.recordEvents.description")));
        features.put("recordTaskInVars", new BooleanMetaType(desc("doc.configuration.events.recordTaskInVars.description")));
        features.put("truncateMaxStringLength", new IntegerMetaType(desc("doc.configuration.events.truncateMaxStringLength.description")));
        features.put("truncateMaxArrayLength", new IntegerMetaType(desc("doc.configuration.events.truncateMaxArrayLength.description")));
        features.put("truncateMaxDepth", new IntegerMetaType(desc("doc.configuration.events.truncateMaxDepth.description")));
        features.put("recordTaskOutVars", new BooleanMetaType(desc("doc.configuration.events.recordTaskOutVars.description")));
        features.put("truncateInVars", new BooleanMetaType(desc("doc.configuration.events.truncateInVars.description")));
        features.put("truncateOutVars", new BooleanMetaType(desc("doc.configuration.events.truncateOutVars.description")));
        features.put("inVarsBlacklist", new StringArrayMetaType(desc("doc.configuration.events.inVarsBlacklist.description")));
        features.put("outVarsBlacklist", new StringArrayMetaType(desc("doc.configuration.events.outVarsBlacklist.description")));
        features.put("recordTaskMeta", new BooleanMetaType(desc("doc.configuration.events.recordTaskMeta.description")));
        features.put("truncateMeta", new BooleanMetaType(desc("doc.configuration.events.truncateMeta.description")));
        features.put("metaBlacklist", new StringArrayMetaType(desc("doc.configuration.events.metaBlacklist.description")));
    }

    private EventsMetaType() {
        super(desc("doc.configuration.events.description"));
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
}
