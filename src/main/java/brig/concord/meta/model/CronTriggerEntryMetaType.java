package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CronTriggerEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final CronTriggerEntryMetaType INSTANCE = new CronTriggerEntryMetaType();

    public static CronTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("spec", "entryPoint");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "spec", StringMetaType::getInstance,
            "entryPoint", CallMetaType::getInstance,
            "runAs", RunAsMetaType::getInstance,
            "activeProfiles", StringArrayMetaType::getInstance,
            "timezone", TimezoneMetaType::getInstance,
            "arguments", AnyMapMetaType::getInstance,
            "exclusive", TriggerExclusiveMetaType::getInstance
    );

    protected CronTriggerEntryMetaType() {
        super("cron trigger entry");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return required;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    private static class RunAsMetaType extends ConcordMetaType {

        private static final RunAsMetaType INSTANCE = new RunAsMetaType();

        public static RunAsMetaType getInstance() {
            return INSTANCE;
        }

        public RunAsMetaType() {
            super("Run As");
        }

        private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
                "withSecret", StringMetaType::getInstance
        );

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            return features;
        }
    }
}
