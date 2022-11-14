package brig.concord.meta.model;

import brig.concord.meta.ConcordMapMetaType;
import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.function.Supplier;

public class ProfilesMetaType extends ConcordMapMetaType {

    private static final ProfilesMetaType INSTANCE = new ProfilesMetaType();

    public static ProfilesMetaType getInstance() {
        return INSTANCE;
    }

    protected ProfilesMetaType() {
        super("Profiles");
    }

    @Override
    protected YamlMetaType getMapEntryType(String name) {
        return ProfilesEntryMetaType.getInstance();
    }

    private static class ProfilesEntryMetaType extends ConcordMetaType {

        private static final ProfilesEntryMetaType INSTANCE = new ProfilesEntryMetaType();

        private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
                "configuration", ConfigurationMetaType::getInstance,
                "flows", FlowsMetaType::getInstance,
                "forms", FormsMetaType::getInstance
        );

        public static ProfilesEntryMetaType getInstance() {
            return INSTANCE;
        }

        private ProfilesEntryMetaType() {
            super("Profile entry");
        }

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            return features;
        }
    }
}
