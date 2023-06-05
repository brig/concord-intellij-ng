package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.model.call.CallMetaType;
import org.jetbrains.yaml.meta.model.YamlArrayType;
import org.jetbrains.yaml.meta.model.YamlBooleanType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public class GitTriggerEntryMetaType extends ConcordMetaType {

    private static final GitTriggerEntryMetaType INSTANCE = new GitTriggerEntryMetaType();

    public static GitTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("entryPoint", "conditions", "version");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "entryPoint", CallMetaType::getInstance,
            "useInitiator", YamlBooleanType::getSharedInstance,
            "activeProfiles", StringArrayMetaType::getInstance,
            "useEventCommitId", YamlBooleanType::getSharedInstance,
            "ignoreEmptyPush", YamlBooleanType::getSharedInstance,
            "arguments", AnyMapMetaType::getInstance,
            "exclusive", TriggerExclusiveMetaType::getInstance,
            "conditions", ConditionsMetaType::getInstance,
            "version", IntegerMetaType::getInstance
    );

    protected GitTriggerEntryMetaType() {
        super("git trigger entry");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return required;
    }

    private static class RepositoryInfoMetaType extends ConcordMetaType {

        private static final RepositoryInfoMetaType INSTANCE = new RepositoryInfoMetaType();

        public static RepositoryInfoMetaType getInstance() {
            return INSTANCE;
        }

        public RepositoryInfoMetaType() {
            super("Repository Info");
        }

        private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
                "repositoryId", RegexpMetaType::getInstance,
                "repository", RegexpMetaType::getInstance,
                "projectId", RegexpMetaType::getInstance,
                "branch", RegexpMetaType::getInstance,
                "enabled", YamlBooleanType::getSharedInstance);

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            return features;
        }
    }

    private static class FilesMetaType extends ConcordMetaType {

        private static final FilesMetaType INSTANCE = new FilesMetaType();

        public static FilesMetaType getInstance() {
            return INSTANCE;
        }

        public FilesMetaType() {
            super("Files Info");
        }

        private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
                "added", RegexpOrArrayMetaType::getInstance,
                "removed", RegexpOrArrayMetaType::getInstance,
                "modified", RegexpOrArrayMetaType::getInstance,
                "any", RegexpOrArrayMetaType::getInstance
        );

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            return features;
        }
    }

    private static class ConditionsMetaType extends ConcordMetaType {

        private static final ConditionsMetaType INSTANCE = new ConditionsMetaType();

        static ConditionsMetaType getInstance() {
            return INSTANCE;
        }

        public ConditionsMetaType() {
            super("Conditions");
        }

        private static final Set<String> required = Set.of("type");

        private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
                "type", StringMetaType::getInstance,
                "githubHost", RegexpMetaType::getInstance,
                "githubOrg", RegexpMetaType::getInstance,
                "githubRepo", RegexpMetaType::getInstance,
                "branch", RegexpMetaType::getInstance,
                "sender", RegexpMetaType::getInstance,
                "status", RegexpMetaType::getInstance,
                "repositoryInfo", () -> new YamlArrayType(RepositoryInfoMetaType.getInstance()),
                "files", FilesMetaType::getInstance,
                "payload", AnyMapMetaType::getInstance
        );

        @Override
        public Map<String, Supplier<YamlMetaType>> getFeatures() {
            return features;
        }

        @Override
        protected Set<String> getRequiredFields() {
            return required;
        }
    }
}
