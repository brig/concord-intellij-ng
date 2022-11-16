package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class CronTriggerEntryMetaType extends ConcordMetaType {

    private static final CronTriggerEntryMetaType INSTANCE = new CronTriggerEntryMetaType();

    public static CronTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("spec", "entryPoint");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "spec", StringMetaType::getInstance,
            "entryPoint", StringMetaType::getInstance,
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
