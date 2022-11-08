package brig.concord.meta.model;

import brig.concord.meta.ConcordAnyMapMetaType;
import brig.concord.meta.ConcordMetaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlNumberType;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;

public class RetryMetaType extends ConcordMetaType {

    private static final RetryMetaType INSTANCE = new RetryMetaType();

    private static final Set<String> requiredFeatures = Collections.emptySet();

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "in", ConcordAnyMapMetaType::getInstance,
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
            super("times [int|expression]", List.of(ExpressionMetaType.getInstance(), YamlNumberType.getInstance(false)));
        }
    }

    private static class DelayType extends YamlAnyOfType {

        private static final DelayType INSTANCE = new DelayType();

        public static DelayType getInstance() {
            return INSTANCE;
        }

        protected DelayType() {
            super("delay [int|expression]", List.of(ExpressionMetaType.getInstance(), YamlNumberType.getInstance(false)));
        }
    }
}
