package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlArrayType;

public class StringArrayMetaType extends YamlArrayType {

    private static final StringArrayMetaType INSTANCE = new StringArrayMetaType();

    public static StringArrayMetaType getInstance() {
        return INSTANCE;
    }

    public StringArrayMetaType() {
        super(StringMetaType.getInstance());
    }
}
