package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlArrayType;
import org.jetbrains.yaml.meta.model.YamlStringType;

public class StringArrayMetaType extends YamlArrayType {

    private static final StringArrayMetaType INSTANCE = new StringArrayMetaType();

    public static StringArrayMetaType getInstance() {
        return INSTANCE;
    }

    public StringArrayMetaType() {
        super(YamlStringType.getInstance());
    }
}
