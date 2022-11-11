package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.ConcordStringArrayMetaType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.Map;
import java.util.function.Supplier;

public class ResourcesMetaType extends ConcordMetaType {

    private static final ResourcesMetaType INSTANCE = new ResourcesMetaType();

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "concord", ConcordStringArrayMetaType::getInstance
    );

    public static ResourcesMetaType getInstance() {
        return INSTANCE;
    }

    private ResourcesMetaType() {
        super("Resources");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }
}
