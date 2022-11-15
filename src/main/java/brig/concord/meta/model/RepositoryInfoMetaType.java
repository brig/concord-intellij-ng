package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class RepositoryInfoMetaType extends ConcordMetaType {

    private static final RepositoryInfoMetaType INSTANCE = new RepositoryInfoMetaType();

    public static RepositoryInfoMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = new HashMap<>();

    static {
        features.put("projectId", YamlStringType::getInstance);
        features.put("repositoryId", YamlStringType::getInstance);
        features.put("repository", YamlStringType::getInstance);
        features.put("branch", YamlStringType::getInstance);
    }
    private RepositoryInfoMetaType() {
        super("RepositoryInfo");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
