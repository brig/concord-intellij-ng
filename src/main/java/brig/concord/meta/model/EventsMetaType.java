package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlBooleanType;
import org.jetbrains.yaml.meta.model.YamlEnumType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EventsMetaType extends ConcordMetaType {

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
        features.put("metaBlacklist", StringMetaType::getInstance);
    }

    public static EventsMetaType getInstance() {
        return INSTANCE;
    }

    protected EventsMetaType() {
        super("loop");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
