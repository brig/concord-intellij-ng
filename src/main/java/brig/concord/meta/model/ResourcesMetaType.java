package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.StringArrayMetaType;

import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ResourcesMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ResourcesMetaType INSTANCE = new ResourcesMetaType();

    private static final Map<String, YamlMetaType> features = Map.of(
            "concord", doc(new StringArrayMetaType(), "doc.resources.concord")
    );

    public static ResourcesMetaType getInstance() {
        return INSTANCE;
    }

    private ResourcesMetaType() {
        setDocBundlePrefix("doc.resources");
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KIND;
    }

    private static <T extends YamlMetaType> T doc(T type, String prefix) {
        type.setDocBundlePrefix(prefix);
        return type;
    }
}
