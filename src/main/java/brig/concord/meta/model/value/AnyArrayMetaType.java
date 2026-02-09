package brig.concord.meta.model.value;

import brig.concord.yaml.meta.model.YamlArrayType;

public class AnyArrayMetaType extends YamlArrayType {

    private static final AnyArrayMetaType INSTANCE = new AnyArrayMetaType();

    public static AnyArrayMetaType getInstance() {
        return INSTANCE;
    }

    public AnyArrayMetaType() {
        super(AnythingMetaType.getInstance());
    }
}
