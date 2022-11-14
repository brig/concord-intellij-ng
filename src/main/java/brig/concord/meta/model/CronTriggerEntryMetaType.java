package brig.concord.meta.model;

import brig.concord.meta.ConcordAnyMapMetaType;
import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlEnumType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

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
            "spec", YamlStringType::getInstance,
            "entryPoint", YamlStringType::getInstance,
            "runAs", RunAsMetaType::getInstance,
            "activeProfiles", StringArrayMetaType::getInstance,
            "timezone", TimezoneMetaType::getInstance,
            "arguments", ConcordAnyMapMetaType::getInstance,
            "exclusive", ExclusiveMetaType::getInstance
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

    private static class ModeType extends YamlEnumType {

        private static final ModeType INSTANCE = new ModeType();

        protected static ModeType getInstance() {
            return INSTANCE;
        }

        protected ModeType() {
            super("Mode");
            setDisplayName("[cancel|cancelOld|wait]");
            withLiterals("cancel", "cancelOld", "wait");
        }
    }

    private static class ExclusiveMetaType extends ConcordMetaType {

        private static final ExclusiveMetaType INSTANCE = new ExclusiveMetaType();

        public static ExclusiveMetaType getInstance() {
            return INSTANCE;
        }

        private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
                "group", YamlStringType::getInstance,
                "groupBy", () -> new YamlEnumType("group by").withLiterals("branch"),
                "mode", ModeType::getInstance
        );

        protected ExclusiveMetaType() {
            super("Exclusive");
        }

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            return features;
        }
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
                "withSecret", YamlStringType::getInstance
        );

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            return features;
        }
    }
}
