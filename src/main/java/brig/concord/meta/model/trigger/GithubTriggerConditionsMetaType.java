package brig.concord.meta.model.trigger;

import brig.concord.meta.ConcordAnyMapMetaType;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.model.RepositoryInfoMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class GithubTriggerConditionsMetaType extends ConcordMetaType {

    private static final GithubTriggerConditionsMetaType INSTANCE = new GithubTriggerConditionsMetaType();

    public static GithubTriggerConditionsMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> requiredFields = Set.of("type");

    private static final Map<String, Supplier<YamlMetaType>> features = new HashMap<>();

    static {
        features.put("type", YamlStringType::getInstance);
        features.put("githubOrg", YamlStringType::getInstance);
        features.put("githubRepo", YamlStringType::getInstance);
        features.put("githubHost", YamlStringType::getInstance);
        features.put("branch", YamlStringType::getInstance);
        features.put("sender", YamlStringType::getInstance);
        features.put("status", YamlStringType::getInstance);
        features.put("repositoryInfo", RepositoryInfoMetaType::getInstance);
        features.put("payload", ConcordAnyMapMetaType::getInstance);
    }

    private GithubTriggerConditionsMetaType() {
        super("GitHubTriggerConditions");
    }

    @Override
    protected Set<String> getRequiredFields() {
        return requiredFields;
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
