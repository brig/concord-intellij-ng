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
import java.util.function.Supplier;

public class LoopMetaType extends ConcordMetaType implements HighlightProvider  {

    private static final LoopMetaType INSTANCE = new LoopMetaType();

    private static final Set<String> requiredFeatures = Set.of("items");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "items", LoopItemsMetaType::getInstance,
            "parallelism", () -> AnyOfType.anyOf(IntegerMetaType.getInstance(), ExpressionMetaType.getInstance()),
            "mode", ModeType::getInstance
    );

    public static LoopMetaType getInstance() {
        return INSTANCE;
    }

    protected LoopMetaType() {
        super("loop");
    }

    @Override
    protected @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
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

        protected ModeType() {
            super("Mode", "[serial|parallel]");
            withLiterals("serial", "parallel");
        }
    }
}
