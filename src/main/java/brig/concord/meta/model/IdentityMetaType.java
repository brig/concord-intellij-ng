package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import brig.concord.yaml.meta.model.YamlMetaType;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

public abstract class IdentityMetaType extends ConcordMetaType {

    private final String identity;
    private final Set<String> requiredFeatures;

    protected IdentityMetaType(String identity, Set<String> requiredFeatures) {
        this.identity = identity;
        this.requiredFeatures = requiredFeatures;
    }

    public String getIdentity() {
        return identity;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return requiredFeatures;
    }

    @Override
    protected abstract @NotNull Map<String, YamlMetaType> getFeatures();
}
