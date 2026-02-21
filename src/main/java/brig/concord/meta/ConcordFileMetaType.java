// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.model.*;
import brig.concord.meta.model.value.StringArrayMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ConcordFileMetaType extends ConcordMetaType implements HighlightProvider {

    public static final String FLOWS_KEY = "flows";

    private static final ConcordFileMetaType INSTANCE = new ConcordFileMetaType();

    public static ConcordFileMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "resources", ResourcesMetaType.getInstance(),
            "configuration", ConfigurationMetaType.getInstance(),
            "publicFlows", new StringArrayMetaType(descKey("doc.publicFlows.description")),
            "forms", FormsMetaType.getInstance(),
            "imports", ImportsMetaType.getInstance(),
            "profiles", ProfilesMetaType.getInstance(),
            FLOWS_KEY, FlowsMetaType.getInstance(),
            "triggers", TriggersMetaType.getInstance()
    );

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_SECTION;
    }
}
