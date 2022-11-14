package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlArrayType;

public class RegexpArrayMetaType extends YamlArrayType {

    private static final RegexpArrayMetaType INSTANCE = new RegexpArrayMetaType();

    public static RegexpArrayMetaType getInstance() {
        return INSTANCE;
    }

    public RegexpArrayMetaType() {
        super(RegexpMetaType.getInstance());
    }
}
