package brig.concord.meta;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.model.*;
import brig.concord.meta.model.value.*;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ConcordFileMetaType extends ConcordMetaType implements HighlightProvider {

    public static final String FLOWS_KEY = "flows";

    private static final ConcordFileMetaType INSTANCE = new ConcordFileMetaType();

    public static ConcordFileMetaType getInstance() {
        return INSTANCE;
    }

    protected ConcordFileMetaType() {
    }

    protected static final Map<String, YamlMetaType> features = new HashMap<>();

    static {
        features.put("resources", ResourcesMetaType.getInstance());
        features.put("configuration", ConfigurationMetaType.getInstance());
        features.put("publicFlows", StringArrayMetaType.getInstance());
        features.put("forms", FormsMetaType.getInstance());
        features.put("imports", ImportsMetaType.getInstance());
        features.put("profiles", ProfilesMetaType.getInstance());
        features.put(FLOWS_KEY, FlowsMetaType.getInstance());
        features.put("triggers", TriggersMetaType.getInstance());
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_SECTION;
    }
}
