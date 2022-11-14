package brig.concord.meta.model;

import brig.concord.meta.ConcordMapMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.function.Supplier;

public class ProfilesMetaType extends ConcordMapMetaType {

    private static final ProfilesMetaType INSTANCE = new ProfilesMetaType();

    private static final Map<String, Supplier<YamlMetaType>> features  = Map.of(
            "profileName", ProfileMetaType::getInstance
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

    @Override
    protected YamlMetaType getMapEntryType(String name) {
        return ProfileMetaType.getInstance();
    }
}
