package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.ExpressionMetaType;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlIntegerType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLValue;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class RetryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final RetryMetaType INSTANCE = new RetryMetaType();

    private static final Map<String, YamlMetaType> features = Map.of(
            "in", new AnyMapMetaType().withDescriptionKey("doc.step.feature.retry.in.description"),
            "times", TimesType.getInstance(),
            "delay", DelayType.getInstance()
    );

    public static RetryMetaType getInstance() {
        return INSTANCE;
    }

    protected RetryMetaType() {
        setDescriptionKey("doc.step.feature.retry.description");
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    private static class TimesType extends YamlAnyOfType {

        private static final TimesType INSTANCE = new TimesType();

        public static TimesType getInstance() {
            return INSTANCE;
        }

        protected TimesType() {
            super(ExpressionMetaType.getInstance(), YamlIntegerType.getInstance(false));

            setDescriptionKey("doc.step.feature.retry.times.description");
        }

        @Override
        public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
            super.validateValue(value, problemsHolder);
        }
    }

    private static class DelayType extends YamlAnyOfType {

        private static final DelayType INSTANCE = new DelayType();

        public static DelayType getInstance() {
            return INSTANCE;
        }

        protected DelayType() {
            super(ExpressionMetaType.getInstance(), YamlIntegerType.getInstance(false));

            setDescriptionKey("doc.step.feature.retry.delay.description");
        }
    }
}
