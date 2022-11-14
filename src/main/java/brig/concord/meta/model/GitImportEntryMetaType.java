package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class GitImportEntryMetaType extends ConcordMetaType {

    private static final GitImportEntryMetaType INSTANCE = new GitImportEntryMetaType();

    public static GitImportEntryMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "name", YamlStringType::getInstance,
            "url", YamlStringType::getInstance,
            "version", YamlStringType::getInstance,
            "path", YamlStringType::getInstance,
            "dest", YamlStringType::getInstance,
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
                "org", YamlStringType::getInstance,
                "name", YamlStringType::getInstance,
                "password", YamlStringType::getInstance
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
