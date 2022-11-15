package brig.concord.meta.model.trigger;

import brig.concord.meta.ConcordAnyMapMetaType;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.ConcordStringArrayMetaType;
import brig.concord.meta.model.ExclusiveMetaType;
import org.jetbrains.yaml.meta.model.YamlBooleanType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.meta.model.YamlStringType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class GitHubTriggerMetaType extends ConcordMetaType {

    private static final GitHubTriggerMetaType INSTANCE = new GitHubTriggerMetaType();

    public static GitHubTriggerMetaType getInstance() {
        return INSTANCE;
    }

    private static final Set<String> requiredFields = Set.of("entryPoint", "conditions");

    private static final Map<String, Supplier<YamlMetaType>> features = new HashMap<>();

    static {
        features.put("entryPoint", YamlStringType::getInstance);
        features.put("useInitiator", YamlBooleanType::getSharedInstance);
        features.put("useEventCommitId", YamlBooleanType::getSharedInstance);
        features.put("ignoreEmptyPush", YamlBooleanType::getSharedInstance);
        features.put("activeProfiles", ConcordStringArrayMetaType::getInstance);
        features.put("exclusive", ExclusiveMetaType::getInstance);
        features.put("arguments", ConcordAnyMapMetaType::getInstance);
        features.put("conditions", GithubTriggerConditionsMetaType::getInstance);
    }

    @Override
    protected Set<String> getRequiredFields() {
        return requiredFields;
    }
    private GitHubTriggerMetaType() {
        super("GitHub");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
