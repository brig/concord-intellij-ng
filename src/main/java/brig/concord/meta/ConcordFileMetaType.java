package brig.concord.meta;

import brig.concord.meta.model.*;
import brig.concord.yaml.meta.model.YamlMetaType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ConcordFileMetaType extends ConcordMetaType {

    private static final ConcordFileMetaType INSTANCE = new ConcordFileMetaType();

    public static ConcordFileMetaType getInstance() {
        return INSTANCE;
    }

    protected ConcordFileMetaType() {
        super("Concord File");
    }

    protected static final Map<String, Supplier<YamlMetaType>> features = new HashMap<>();

    static {
        features.put("resources", ResourcesMetaType::getInstance);
        features.put("configuration", ConfigurationMetaType::getInstance);
        features.put("publicFlows", StringArrayMetaType::getInstance);
        features.put("forms", FormsMetaType::getInstance);
        features.put("imports", ImportsMetaType::getInstance);
        features.put("profiles", ProfilesMetaType::getInstance);
        features.put("flows", FlowsMetaType::getInstance);
        features.put("triggers", TriggersMetaType::getInstance);
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
