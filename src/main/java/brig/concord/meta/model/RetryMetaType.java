package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.yaml.meta.model.YamlIntegerType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class RetryMetaType extends ConcordMetaType {

    private static final RetryMetaType INSTANCE = new RetryMetaType();

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "in", AnyMapMetaType::getInstance,
            "times", TimesType::getInstance,
            "delay", DelayType::getInstance
    );

    public static RetryMetaType getInstance() {
        return INSTANCE;
    }

    protected RetryMetaType() {
        super("retry");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    private static class TimesType extends YamlAnyOfType {

        private static final TimesType INSTANCE = new TimesType();

        public static TimesType getInstance() {
            return INSTANCE;
        }

        protected TimesType() {
            super("times", List.of(ExpressionMetaType.getInstance(), YamlIntegerType.getInstance(false)));
            setDisplayName("[int|expression]");
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
            super("delay", List.of(ExpressionMetaType.getInstance(), YamlIntegerType.getInstance(false)));
            setDisplayName("[int|expression]");
        }
    }
}
