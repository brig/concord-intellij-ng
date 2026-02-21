// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
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

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class EventsMetaType extends ConcordMetaType implements HighlightProvider {

    private static final EventsMetaType INSTANCE = new EventsMetaType();

    private static final Map<String, YamlMetaType> features = new HashMap<>();

    static {
        features.put("recordEvents", new BooleanMetaType(descKey("doc.configuration.events.recordEvents.description")));
        features.put("recordTaskInVars", new BooleanMetaType(descKey("doc.configuration.events.recordTaskInVars.description")));
        features.put("truncateMaxStringLength", new IntegerMetaType(descKey("doc.configuration.events.truncateMaxStringLength.description")));
        features.put("truncateMaxArrayLength", new IntegerMetaType(descKey("doc.configuration.events.truncateMaxArrayLength.description")));
        features.put("truncateMaxDepth", new IntegerMetaType(descKey("doc.configuration.events.truncateMaxDepth.description")));
        features.put("recordTaskOutVars", new BooleanMetaType(descKey("doc.configuration.events.recordTaskOutVars.description")));
        features.put("truncateInVars", new BooleanMetaType(descKey("doc.configuration.events.truncateInVars.description")));
        features.put("truncateOutVars", new BooleanMetaType(descKey("doc.configuration.events.truncateOutVars.description")));
        features.put("inVarsBlacklist", new StringArrayMetaType(descKey("doc.configuration.events.inVarsBlacklist.description")));
        features.put("outVarsBlacklist", new StringArrayMetaType(descKey("doc.configuration.events.outVarsBlacklist.description")));
        features.put("recordTaskMeta", new BooleanMetaType(descKey("doc.configuration.events.recordTaskMeta.description")));
        features.put("truncateMeta", new BooleanMetaType(descKey("doc.configuration.events.truncateMeta.description")));
        features.put("metaBlacklist", new StringArrayMetaType(descKey("doc.configuration.events.metaBlacklist.description")));
    }

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
