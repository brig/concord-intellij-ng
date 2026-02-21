// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.MapMetaType;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLMapping;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class FlowsMetaType extends MapMetaType implements HighlightProvider {

    private static final FlowsMetaType INSTANCE = new FlowsMetaType();

    public static FlowsMetaType getInstance() {
        return INSTANCE;
    }

    private static final List<Field> defaultCompletions = List.of(new Field("default", StepsMetaType.getInstance()));

    private FlowsMetaType() {
        super(descKey("doc.flows.description"));
    }

    @Override
    public @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
        return defaultCompletions;
    }

    @Override
    protected YamlMetaType getMapEntryType(String name) {
        return StepsMetaType.getInstance();
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.FLOW_IDENTIFIER;
    }
}
