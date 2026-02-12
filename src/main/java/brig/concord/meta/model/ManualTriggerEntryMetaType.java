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
import java.util.Set;

public class ManualTriggerEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ManualTriggerEntryMetaType INSTANCE = new ManualTriggerEntryMetaType();

    public static ManualTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("entryPoint");

    private static final Map<String, YamlMetaType> features = Map.of(
            "name", new StringMetaType().withDescriptionKey("doc.triggers.manual.name.description"),
            "entryPoint", new CallMetaType().withDescriptionKey("doc.triggers.manual.entryPoint.description"),
            "activeProfiles", new StringArrayMetaType().withDescriptionKey("doc.triggers.manual.activeProfiles.description"),
            "arguments", new AnyMapMetaType().withDescriptionKey("doc.triggers.manual.arguments.description"),
            "exclusive", TriggerExclusiveMetaType.getInstance()
    );

    protected ManualTriggerEntryMetaType() {
        setDescriptionKey("doc.triggers.manual.description");
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
