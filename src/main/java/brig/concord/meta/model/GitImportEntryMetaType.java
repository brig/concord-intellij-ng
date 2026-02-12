package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.RegexpArrayMetaType;
import brig.concord.meta.model.value.StringMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class GitImportEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final GitImportEntryMetaType INSTANCE = new GitImportEntryMetaType();

    public static GitImportEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "name", new StringMetaType().withDescriptionKey("doc.imports.git.name.description"),
            "url", new StringMetaType().withDescriptionKey("doc.imports.git.url.description"),
            "version", new StringMetaType().withDescriptionKey("doc.imports.git.version.description"),
            "path", new StringMetaType().withDescriptionKey("doc.imports.git.path.description"),
            "dest", new StringMetaType().withDescriptionKey("doc.imports.git.dest.description"),
            "exclude", new RegexpArrayMetaType().withDescriptionKey("doc.imports.git.exclude.description"),
            "secret", new SecretMetaType().withDescriptionKey("doc.imports.git.secret.description")
    );

    protected GitImportEntryMetaType() {
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    private static class SecretMetaType extends ConcordMetaType implements HighlightProvider {

        private static final SecretMetaType INSTANCE = new SecretMetaType();

        public static SecretMetaType getInstance() {
            return INSTANCE;
        }

        private static final Set<String> required = Set.of("name");

        private static final Map<String, YamlMetaType> features = Map.of(
                "org", new StringMetaType().withDescriptionKey("doc.imports.git.secret.org.description"),
                "name", new StringMetaType().withDescriptionKey("doc.imports.git.secret.name.description"),
                "password", new StringMetaType().withDescriptionKey("doc.imports.git.secret.password.description")
        );

        protected SecretMetaType() {
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
        public TextAttributesKey getKeyHighlight(String key) {
            return ConcordHighlightingColors.DSL_KEY;
        }
    }
}
