package brig.concord.meta.model;

import brig.concord.meta.ConcordIntegerMetaType;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.ConcordStringArrayMetaType;
import org.jetbrains.yaml.meta.model.YamlBooleanType;
import org.jetbrains.yaml.meta.model.YamlEnumType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class EventsMetaType extends ConcordMetaType {

    private static final EventsMetaType INSTANCE = new EventsMetaType();

    private static final Map<String, Supplier<YamlMetaType>> features = new HashMap<>();

    static {
        features.put("recordEvents", YamlBooleanType::getSharedInstance);
        features.put("recordTaskInVars", YamlBooleanType::getSharedInstance);
        features.put("truncateMaxStringLength", ConcordIntegerMetaType::getInstance);
        features.put("truncateMaxArrayLength", ConcordIntegerMetaType::getInstance);
        features.put("truncateMaxDepth", ConcordIntegerMetaType::getInstance);
        features.put("recordTaskOutVars", YamlBooleanType::getSharedInstance);
        features.put("truncateOutVars", YamlBooleanType::getSharedInstance);
        features.put("inVarsBlacklist", ConcordStringArrayMetaType::getInstance);
        features.put("outVarsBlacklist", ConcordStringArrayMetaType::getInstance);
        features.put("recordTaskMeta", YamlBooleanType::getSharedInstance);
        features.put("truncateMeta", YamlBooleanType::getSharedInstance);
        features.put("metaBlacklist", YamlStringType::getInstance);
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

    private static class ModeType extends YamlEnumType {

        private static final ModeType INSTANCE = new ModeType();

        public static ModeType getInstance() {
            return INSTANCE;
        }

        protected ModeType() {
            super("Mode");
            setDisplayName("[serial|parallel]");
            withLiterals("serial", "parallel");
        }
    }
}
