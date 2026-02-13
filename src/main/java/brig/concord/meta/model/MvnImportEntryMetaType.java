package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.StringMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class MvnImportEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final MvnImportEntryMetaType INSTANCE = new MvnImportEntryMetaType();

    public static MvnImportEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("url");

    private static final Map<String, YamlMetaType> features = Map.of(
            "url", new StringMetaType().withDescriptionKey("doc.imports.mvn.url.description"),
            "dest", new StringMetaType().withDescriptionKey("doc.imports.mvn.dest.description")
    );

    private MvnImportEntryMetaType() {
        setDescriptionKey("doc.imports.mvn.description");
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
}
