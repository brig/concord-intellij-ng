// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.StringArrayMetaType;
import brig.concord.meta.model.value.StringMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ManualTriggerEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ManualTriggerEntryMetaType INSTANCE = new ManualTriggerEntryMetaType();

    public static ManualTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = Map.of(
            "name", new StringMetaType(descKey("doc.triggers.manual.name.description")),
            "entryPoint", new CallMetaType(descKey("doc.triggers.manual.entryPoint.description").andRequired()),
            "activeProfiles", new StringArrayMetaType(descKey("doc.triggers.manual.activeProfiles.description")),
            "arguments", new AnyMapMetaType(descKey("doc.triggers.manual.arguments.description")),
            "exclusive", TriggerExclusiveMetaType.getInstance()
    );

    private ManualTriggerEntryMetaType() {
        super(descKey("doc.triggers.manual.description"));
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
