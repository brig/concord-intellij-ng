package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.function.Supplier;

public class ProfilesMetaType extends ConcordMetaType {

    private static final ProfilesMetaType INSTANCE = new ProfilesMetaType();

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "configuration", ConfigurationMetaType::getInstance,
            "flows", FlowsMetaType::getInstance,
            "forms", FormsMetaType::getInstance
    );

    public static ProfilesMetaType getInstance() {
        return INSTANCE;
    }

    private ProfilesMetaType() {
        super("Profiles");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
