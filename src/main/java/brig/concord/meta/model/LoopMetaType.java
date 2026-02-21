// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.AnyOfType;
import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.meta.model.value.IntegerMetaType;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class LoopMetaType extends ConcordMetaType implements HighlightProvider  {

    private static final LoopMetaType INSTANCE = new LoopMetaType();

    private static final Map<String, YamlMetaType> features = Map.of(
            "items", LoopItemsMetaType.getInstance(),
            "parallelism", AnyOfType.anyOf(descKey("doc.step.feature.loop.parallelism.description"),
                    IntegerMetaType.getInstance(), ExpressionMetaType.getInstance()),
            "mode", ModeType.getInstance()
    );

    public static LoopMetaType getInstance() {
        return INSTANCE;
    }

    private LoopMetaType() {
        super(descKey("doc.step.feature.loop.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    private static class ModeType extends YamlEnumType {

        private static final ModeType INSTANCE = new ModeType();

        public static ModeType getInstance() {
            return INSTANCE;
        }

        private ModeType() {
            super("string", descKey("doc.step.feature.loop.mode.description"),
                    List.of(
                            EnumValue.ofKey("serial", "doc.step.feature.loop.mode.serial.description"),
                            EnumValue.ofKey("parallel", "doc.step.feature.loop.mode.parallel.description")));
        }
    }
}
