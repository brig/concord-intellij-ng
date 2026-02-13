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

import java.util.Map;
import java.util.Set;

public class LoopMetaType extends ConcordMetaType implements HighlightProvider  {

    private static final LoopMetaType INSTANCE = new LoopMetaType();

    private static final Set<String> requiredFeatures = Set.of("items");

    private static final Map<String, YamlMetaType> features = Map.of(
            "items", LoopItemsMetaType.getInstance(),
            "parallelism", AnyOfType.anyOf(IntegerMetaType.getInstance(), ExpressionMetaType.getInstance())
                    .withDescriptionKey("doc.step.feature.loop.parallelism.description"),
            "mode", ModeType.getInstance()
    );

    public static LoopMetaType getInstance() {
        return INSTANCE;
    }

    private LoopMetaType() {
        setDescriptionKey("doc.step.feature.loop.description");
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return requiredFeatures;
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
            super("string");
            setLiterals("serial", "parallel");
            setDescriptionKeys(
                    "doc.step.feature.loop.mode.serial.description",
                    "doc.step.feature.loop.mode.parallel.description");
            setDescriptionKey("doc.step.feature.loop.mode.description");
        }
    }
}
