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
            "entryPoint", new CallMetaType().withDescriptionKey("doc.triggers.github.entryPoint.description"),
            "useInitiator", new BooleanMetaType().withDescriptionKey("doc.triggers.github.useInitiator.description"),
            "activeProfiles", new StringArrayMetaType().withDescriptionKey("doc.triggers.github.activeProfiles.description"),
            "useEventCommitId", new BooleanMetaType().withDescriptionKey("doc.triggers.github.useEventCommitId.description"),
            "ignoreEmptyPush", new BooleanMetaType().withDescriptionKey("doc.triggers.github.ignoreEmptyPush.description"),
            "arguments", new AnyMapMetaType().withDescriptionKey("doc.triggers.github.arguments.description"),
            "exclusive", TriggerExclusiveMetaType.getInstance(),
            "conditions", ConditionsMetaType.getInstance(),
            "version", new IntegerMetaType().withDescriptionKey("doc.triggers.github.version.description")
    );

    private GitTriggerEntryMetaType() {
        setDescriptionKey("doc.triggers.github.description");
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

        private RepositoryInfoMetaType() {
            setDescriptionKey("doc.triggers.github.conditions.repositoryInfo.description");
        }

        private static final Map<String, YamlMetaType> features = Map.of(
                "repositoryId", new RegexpMetaType().withDescriptionKey("doc.triggers.github.conditions.repositoryInfo.repositoryId.description"),
                "repository", new RegexpMetaType().withDescriptionKey("doc.triggers.github.conditions.repositoryInfo.repository.description"),
                "projectId", new RegexpMetaType().withDescriptionKey("doc.triggers.github.conditions.repositoryInfo.projectId.description"),
                "branch", new RegexpMetaType().withDescriptionKey("doc.triggers.github.conditions.repositoryInfo.branch.description"),
                "enabled", new BooleanMetaType().withDescriptionKey("doc.triggers.github.conditions.repositoryInfo.enabled.description"));

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

        private FilesMetaType() {
            setDescriptionKey("doc.triggers.github.conditions.files.description");
        }

        private static final Map<String, YamlMetaType> features = Map.of(
                "added", new RegexpOrArrayMetaType().withDescriptionKey("doc.triggers.github.conditions.files.added.description"),
                "removed", new RegexpOrArrayMetaType().withDescriptionKey("doc.triggers.github.conditions.files.removed.description"),
                "modified", new RegexpOrArrayMetaType().withDescriptionKey("doc.triggers.github.conditions.files.modified.description"),
                "any", new RegexpOrArrayMetaType().withDescriptionKey("doc.triggers.github.conditions.files.any.description")
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

        private ConditionsMetaType() {
            setDescriptionKey("doc.triggers.github.conditions.description");
        }

        private static final Set<String> required = Set.of("type");

        private static final Map<String, YamlMetaType> features = Map.of(
                "type", new StringMetaType().withDescriptionKey("doc.triggers.github.conditions.type.description"),
                "githubHost", new RegexpMetaType().withDescriptionKey("doc.triggers.github.conditions.githubHost.description"),
                "githubOrg", new RegexpMetaType().withDescriptionKey("doc.triggers.github.conditions.githubOrg.description"),
                "githubRepo", new RegexpMetaType().withDescriptionKey("doc.triggers.github.conditions.githubRepo.description"),
                "branch", new RegexpMetaType().withDescriptionKey("doc.triggers.github.conditions.branch.description"),
                "sender", new RegexpMetaType().withDescriptionKey("doc.triggers.github.conditions.sender.description"),
                "status", new RegexpMetaType().withDescriptionKey("doc.triggers.github.conditions.status.description"),
                "repositoryInfo", new YamlArrayType(RepositoryInfoMetaType.getInstance()).withDescriptionKey("doc.triggers.github.conditions.repositoryInfo.description"),
                "files", FilesMetaType.getInstance(),
                "payload", new AnyMapMetaType().withDescriptionKey("doc.triggers.github.conditions.payload.description")
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
