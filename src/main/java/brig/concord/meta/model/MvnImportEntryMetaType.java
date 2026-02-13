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

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class MvnImportEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final MvnImportEntryMetaType INSTANCE = new MvnImportEntryMetaType();

    public static MvnImportEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "url", new StringMetaType(desc("doc.imports.mvn.url.description").andRequired()),
            "dest", new StringMetaType(desc("doc.imports.mvn.dest.description"))
    );

    private MvnImportEntryMetaType() {
        super(desc("doc.imports.mvn.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }
}
