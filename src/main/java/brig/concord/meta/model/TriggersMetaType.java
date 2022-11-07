package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlAnyScalarType;
import org.jetbrains.yaml.meta.model.YamlArrayType;

public class TriggersMetaType extends YamlArrayType {

    private static final TriggersMetaType INSTANCE = new TriggersMetaType();

    public static TriggersMetaType getInstance() {
        return INSTANCE;
    }

    public TriggersMetaType() {
        super(YamlAnyScalarType.getInstance());
    }
}
