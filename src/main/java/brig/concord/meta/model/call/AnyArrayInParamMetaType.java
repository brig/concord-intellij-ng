package brig.concord.meta.model.call;

import org.jetbrains.yaml.meta.model.YamlArrayType;

@SuppressWarnings("UnstableApiUsage")
public class AnyArrayInParamMetaType extends YamlArrayType implements CallInParamMetaType {

    private static final AnyArrayInParamMetaType INSTANCE = new AnyArrayInParamMetaType();

    public AnyArrayInParamMetaType() {
        super(AnyInParamMetaType.getInstance());
    }

    public static AnyArrayInParamMetaType getInstance() {
        return INSTANCE;
    }
}
