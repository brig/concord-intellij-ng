package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ManualTriggerEntryMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ManualTriggerEntryMetaType INSTANCE = new ManualTriggerEntryMetaType();

    public static ManualTriggerEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> required = Set.of("entryPoint");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "name", StringMetaType::getInstance,
            "entryPoint", CallMetaType::getInstance,
            "activeProfiles", StringArrayMetaType::getInstance,
            "arguments", AnyMapMetaType::getInstance
    );

    protected ManualTriggerEntryMetaType() {
        super("manual trigger entry");
    }

    @Override
    protected @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
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
