package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class GitImportEntryMetaType extends ConcordMetaType {

    private static final GitImportEntryMetaType INSTANCE = new GitImportEntryMetaType();

    public static GitImportEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "name", StringMetaType::getInstance,
            "url", StringMetaType::getInstance,
            "version", StringMetaType::getInstance,
            "path", StringMetaType::getInstance,
            "dest", StringMetaType::getInstance,
            "exclude", RegexpArrayMetaType::getInstance,
            "secret", SecretMetaType::getInstance
            );

    protected GitImportEntryMetaType() {
        super("git import entry");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    private static class SecretMetaType extends ConcordMetaType {

        private static final SecretMetaType INSTANCE = new SecretMetaType();

        public static SecretMetaType getInstance() {
            return INSTANCE;
        }

        private static final Set<String> required = Set.of("name");

        private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
                "org", StringMetaType::getInstance,
                "name", StringMetaType::getInstance,
                "password", StringMetaType::getInstance
        );

        protected SecretMetaType() {
            super("Secret");
        }

        @Override
        protected Map<String, Supplier<YamlMetaType>> getFeatures() {
            return features;
        }

        @Override
        protected Set<String> getRequiredFields() {
            return required;
        }
    }
}
