// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.StringArrayMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ResourcesMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ResourcesMetaType INSTANCE = new ResourcesMetaType();

    private static final Map<String, YamlMetaType> features = Map.of(
            "concord", new StringArrayMetaType(descKey("doc.resources.concord.description"))
    );

    public static ResourcesMetaType getInstance() {
        return INSTANCE;
    }

    private ResourcesMetaType() {
        super(descKey("doc.resources.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KIND;
    }
}
