package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlAnyScalarType;
import org.jetbrains.yaml.meta.model.YamlArrayType;

public class ImportsMetaType extends YamlArrayType {

    private static final ImportsMetaType INSTANCE = new ImportsMetaType();

    public static ImportsMetaType getInstance() {
        return INSTANCE;
    }

    public ImportsMetaType() {
        super(YamlAnyScalarType.getInstance());
    }
}
