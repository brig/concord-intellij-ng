package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("UnstableApiUsage")
public abstract class IdentityMetaType extends ConcordMetaType {

    private final String identity;
    private final Set<String> requiredFeatures;

    protected IdentityMetaType(String typeName, String identity, Set<String> requiredFeatures) {
        super(typeName);

        this.identity = identity;
        this.requiredFeatures = requiredFeatures;
    }

    protected String getIdentity() {
        return identity;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return requiredFeatures;
    }

    @Override
    protected abstract Map<String, Supplier<YamlMetaType>> getFeatures();
}
