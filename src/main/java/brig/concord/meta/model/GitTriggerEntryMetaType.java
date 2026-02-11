package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.BooleanMetaType;
import brig.concord.meta.model.value.IntegerMetaType;
import brig.concord.meta.model.value.RegexpMetaType;
import brig.concord.meta.model.value.RegexpOrArrayMetaType;
import brig.concord.meta.model.value.StringArrayMetaType;
import brig.concord.meta.model.value.StringMetaType;

import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class GitTriggerEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final GitTriggerEntryMetaType INSTANCE = new GitTriggerEntryMetaType();

    public static GitTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("entryPoint", "conditions", "version");

    private static final Map<String, YamlMetaType> features = Map.of(
            "entryPoint", CallMetaType.getInstance(),
            "useInitiator", BooleanMetaType.getInstance(),
            "activeProfiles", StringArrayMetaType.getInstance(),
            "useEventCommitId", BooleanMetaType.getInstance(),
            "ignoreEmptyPush", BooleanMetaType.getInstance(),
            "arguments", AnyMapMetaType.getInstance(),
            "exclusive", TriggerExclusiveMetaType.getInstance(),
            "conditions", ConditionsMetaType.getInstance(),
            "version", IntegerMetaType.getInstance()
    );

    protected GitTriggerEntryMetaType() {
        super("git trigger entry");
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
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

    private static class RepositoryInfoMetaType extends ConcordMetaType implements HighlightProvider {

        private static final RepositoryInfoMetaType INSTANCE = new RepositoryInfoMetaType();

        public static RepositoryInfoMetaType getInstance() {
            return INSTANCE;
        }

        public RepositoryInfoMetaType() {
            super("Repository Info");
        }

        private static final Map<String, YamlMetaType> features = Map.of(
                "repositoryId", RegexpMetaType.getInstance(),
                "repository", RegexpMetaType.getInstance(),
                "projectId", RegexpMetaType.getInstance(),
                "branch", RegexpMetaType.getInstance(),
                "enabled", BooleanMetaType.getInstance());

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            return features;
        }

        @Override
        public @Nullable TextAttributesKey getKeyHighlight(String key) {
            return ConcordHighlightingColors.DSL_KEY;
        }
    }

    private static class FilesMetaType extends ConcordMetaType implements HighlightProvider {

        private static final FilesMetaType INSTANCE = new FilesMetaType();

        public static FilesMetaType getInstance() {
            return INSTANCE;
        }

        public FilesMetaType() {
            super("Files Info");
        }

        private static final Map<String, YamlMetaType> features = Map.of(
                "added", RegexpOrArrayMetaType.getInstance(),
                "removed", RegexpOrArrayMetaType.getInstance(),
                "modified", RegexpOrArrayMetaType.getInstance(),
                "any", RegexpOrArrayMetaType.getInstance()
        );

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            return features;
        }

        @Override
        public @Nullable TextAttributesKey getKeyHighlight(String key) {
            return ConcordHighlightingColors.DSL_KEY;
        }
    }

    private static class ConditionsMetaType extends ConcordMetaType implements HighlightProvider {

        private static final ConditionsMetaType INSTANCE = new ConditionsMetaType();

        static ConditionsMetaType getInstance() {
            return INSTANCE;
        }

        public ConditionsMetaType() {
            super("Conditions");
        }

        private static final Set<String> required = Set.of("type");

        private static final Map<String, YamlMetaType> features = Map.of(
                "type", StringMetaType.getInstance(),
                "githubHost", RegexpMetaType.getInstance(),
                "githubOrg", RegexpMetaType.getInstance(),
                "githubRepo", RegexpMetaType.getInstance(),
                "branch", RegexpMetaType.getInstance(),
                "sender", RegexpMetaType.getInstance(),
                "status", RegexpMetaType.getInstance(),
                "repositoryInfo", new YamlArrayType(RepositoryInfoMetaType.getInstance()),
                "files", FilesMetaType.getInstance(),
                "payload", AnyMapMetaType.getInstance()
        );

        @Override
        public @NotNull Map<String, YamlMetaType> getFeatures() {
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
    }
}
