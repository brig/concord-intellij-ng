package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.meta.model.value.*;
import brig.concord.yaml.meta.model.YamlArrayType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class GitTriggerEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final GitTriggerEntryMetaType INSTANCE = new GitTriggerEntryMetaType();

    public static GitTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "entryPoint", new CallMetaType(desc("doc.triggers.github.entryPoint.description").andRequired()),
            "useInitiator", new BooleanMetaType(desc("doc.triggers.github.useInitiator.description")),
            "activeProfiles", new StringArrayMetaType(desc("doc.triggers.github.activeProfiles.description")),
            "useEventCommitId", new BooleanMetaType(desc("doc.triggers.github.useEventCommitId.description")),
            "ignoreEmptyPush", new BooleanMetaType(desc("doc.triggers.github.ignoreEmptyPush.description")),
            "arguments", new AnyMapMetaType(desc("doc.triggers.github.arguments.description")),
            "exclusive", TriggerExclusiveMetaType.getInstance(),
            "conditions", ConditionsMetaType.getInstance(),
            "version", new IntegerMetaType(desc("doc.triggers.github.version.description").andRequired())
    );

    private GitTriggerEntryMetaType() {
        super(desc("doc.triggers.github.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
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
            super(desc("doc.triggers.github.conditions.repositoryInfo.description"));
        }

        private static final Map<String, YamlMetaType> features = Map.of(
                "repositoryId", new RegexpMetaType(desc("doc.triggers.github.conditions.repositoryInfo.repositoryId.description")),
                "repository", new RegexpMetaType(desc("doc.triggers.github.conditions.repositoryInfo.repository.description")),
                "projectId", new RegexpMetaType(desc("doc.triggers.github.conditions.repositoryInfo.projectId.description")),
                "branch", new RegexpMetaType(desc("doc.triggers.github.conditions.repositoryInfo.branch.description")),
                "enabled", new BooleanMetaType(desc("doc.triggers.github.conditions.repositoryInfo.enabled.description")));

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
            super(desc("doc.triggers.github.conditions.files.description"));
        }

        private static final Map<String, YamlMetaType> features = Map.of(
                "added", new RegexpOrArrayMetaType(desc("doc.triggers.github.conditions.files.added.description")),
                "removed", new RegexpOrArrayMetaType(desc("doc.triggers.github.conditions.files.removed.description")),
                "modified", new RegexpOrArrayMetaType(desc("doc.triggers.github.conditions.files.modified.description")),
                "any", new RegexpOrArrayMetaType(desc("doc.triggers.github.conditions.files.any.description"))
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
            super(desc("doc.triggers.github.conditions.description").andRequired());
        }

        private static final Map<String, YamlMetaType> features = Map.of(
                "type", new StringMetaType(desc("doc.triggers.github.conditions.type.description").andRequired()),
                "githubHost", new RegexpMetaType(desc("doc.triggers.github.conditions.githubHost.description")),
                "githubOrg", new RegexpMetaType(desc("doc.triggers.github.conditions.githubOrg.description")),
                "githubRepo", new RegexpMetaType(desc("doc.triggers.github.conditions.githubRepo.description")),
                "branch", new RegexpMetaType(desc("doc.triggers.github.conditions.branch.description")),
                "sender", new RegexpMetaType(desc("doc.triggers.github.conditions.sender.description")),
                "status", new RegexpMetaType(desc("doc.triggers.github.conditions.status.description")),
                "repositoryInfo", new YamlArrayType(RepositoryInfoMetaType.getInstance(), desc("doc.triggers.github.conditions.repositoryInfo.description")),
                "files", FilesMetaType.getInstance(),
                "payload", new AnyMapMetaType(desc("doc.triggers.github.conditions.payload.description"))
        );

        @Override
        public @NotNull Map<String, YamlMetaType> getFeatures() {
            return features;
        }

        @Override
        public @Nullable TextAttributesKey getKeyHighlight(String key) {
            return ConcordHighlightingColors.DSL_KEY;
        }
    }
}
