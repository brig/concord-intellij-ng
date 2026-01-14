package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.yaml.meta.model.YamlBooleanType;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EventsMetaType extends ConcordMetaType implements HighlightProvider {

    private static final EventsMetaType INSTANCE = new EventsMetaType();

    private static final Map<String, Supplier<YamlMetaType>> features = new HashMap<>();

    static {
        features.put("recordEvents", YamlBooleanType::getSharedInstance);
        features.put("recordTaskInVars", YamlBooleanType::getSharedInstance);
        features.put("truncateMaxStringLength", IntegerMetaType::getInstance);
        features.put("truncateMaxArrayLength", IntegerMetaType::getInstance);
        features.put("truncateMaxDepth", IntegerMetaType::getInstance);
        features.put("recordTaskOutVars", YamlBooleanType::getSharedInstance);
        features.put("truncateInVars", YamlBooleanType::getSharedInstance);
        features.put("truncateOutVars", YamlBooleanType::getSharedInstance);
        features.put("inVarsBlacklist", StringArrayMetaType::getInstance);
        features.put("outVarsBlacklist", StringArrayMetaType::getInstance);
        features.put("recordTaskMeta", YamlBooleanType::getSharedInstance);
        features.put("truncateMeta", YamlBooleanType::getSharedInstance);
        features.put("metaBlacklist", StringArrayMetaType::getInstance);
    }

    public static EventsMetaType getInstance() {
        return INSTANCE;
    }

    protected EventsMetaType() {
        super("Events");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }
}
