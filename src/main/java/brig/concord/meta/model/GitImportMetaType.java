package brig.concord.meta.model;

import brig.concord.meta.ConcordRegexpArrayMetaType;
import brig.concord.meta.ConcordRegexpMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class GitImportMetaType extends IdentityMetaType {

    private static final GitImportMetaType INSTANCE = new GitImportMetaType();

    public static GitImportMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "name", YamlStringType::getInstance,
            "url", YamlStringType::getInstance,
            "version", YamlStringType::getInstance,
            "path", YamlStringType::getInstance,
            "dest", YamlStringType::getInstance,
            "exclude", ConcordRegexpArrayMetaType::getInstance
//            "secret",
            );

    protected GitImportMetaType() {
        super("git", "git", Set.of("git"));
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
