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

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class GitImportEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final GitImportEntryMetaType INSTANCE = new GitImportEntryMetaType();

    public static GitImportEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "name", new StringMetaType(descKey("doc.imports.git.name.description")),
            "url", new StringMetaType(descKey("doc.imports.git.url.description")),
            "version", new StringMetaType(descKey("doc.imports.git.version.description")),
            "path", new StringMetaType(descKey("doc.imports.git.path.description")),
            "dest", new StringMetaType(descKey("doc.imports.git.dest.description")),
            "exclude", new RegexpArrayMetaType(descKey("doc.imports.git.exclude.description")),
            "secret", SecretMetaType.getInstance()
    );

    private GitImportEntryMetaType() {
        super(descKey("doc.imports.git.description"));
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

        private static final Map<String, YamlMetaType> features = Map.of(
                "org", new StringMetaType(descKey("doc.imports.git.secret.org.description")),
                "name", new StringMetaType(descKey("doc.imports.git.secret.name.description").andRequired()),
                "password", new StringMetaType(descKey("doc.imports.git.secret.password.description"))
        );

        private SecretMetaType() {
            super(descKey("doc.imports.git.secret.description"));
        }

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            return features;
        }

        @Override
        public TextAttributesKey getKeyHighlight(String key) {
            return ConcordHighlightingColors.DSL_KEY;
        }
    }
}
