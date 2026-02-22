// SPDX-License-Identifier: Apache-2.0
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

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class EventsMetaType extends ConcordMetaType implements HighlightProvider {

    private static final EventsMetaType INSTANCE = new EventsMetaType();

    private static final Map<String, YamlMetaType> features = Map.ofEntries(
            Map.entry("batchSize", new IntegerMetaType(descKey("doc.configuration.events.batchSize.description"))),
            Map.entry("batchFlushInterval", new IntegerMetaType(descKey("doc.configuration.events.batchFlushInterval.description"))),
            Map.entry("recordEvents", new BooleanMetaType(descKey("doc.configuration.events.recordEvents.description"))),
            Map.entry("recordTaskInVars", new BooleanMetaType(descKey("doc.configuration.events.recordTaskInVars.description"))),
            Map.entry("truncateMaxStringLength", new IntegerMetaType(descKey("doc.configuration.events.truncateMaxStringLength.description"))),
            Map.entry("truncateMaxArrayLength", new IntegerMetaType(descKey("doc.configuration.events.truncateMaxArrayLength.description"))),
            Map.entry("truncateMaxDepth", new IntegerMetaType(descKey("doc.configuration.events.truncateMaxDepth.description"))),
            Map.entry("recordTaskOutVars", new BooleanMetaType(descKey("doc.configuration.events.recordTaskOutVars.description"))),
            Map.entry("truncateInVars", new BooleanMetaType(descKey("doc.configuration.events.truncateInVars.description"))),
            Map.entry("truncateOutVars", new BooleanMetaType(descKey("doc.configuration.events.truncateOutVars.description"))),
            Map.entry("inVarsBlacklist", new StringArrayMetaType(descKey("doc.configuration.events.inVarsBlacklist.description"))),
            Map.entry("outVarsBlacklist", new StringArrayMetaType(descKey("doc.configuration.events.outVarsBlacklist.description"))),
            Map.entry("recordTaskMeta", new BooleanMetaType(descKey("doc.configuration.events.recordTaskMeta.description"))),
            Map.entry("truncateMeta", new BooleanMetaType(descKey("doc.configuration.events.truncateMeta.description"))),
            Map.entry("metaBlacklist", new StringArrayMetaType(descKey("doc.configuration.events.metaBlacklist.description")))
    );

    private EventsMetaType() {
        super(descKey("doc.configuration.events.description"));
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
