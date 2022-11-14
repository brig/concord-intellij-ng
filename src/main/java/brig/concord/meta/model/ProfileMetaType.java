package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.function.Supplier;

public class ProfileMetaType extends ConcordMetaType {
    private static final ProfileMetaType INSTANCE = new ProfileMetaType();

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "configuration", ConfigurationMetaType::getInstance,
            "flows", FlowsMetaType::getInstance,
            "forms", FormsMetaType::getInstance
    );

    public static ProfileMetaType getInstance() {
        return INSTANCE;
    }

    private ProfileMetaType() {
        super("Profile");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
