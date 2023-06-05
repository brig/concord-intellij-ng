package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlArrayType;

@SuppressWarnings("UnstableApiUsage")
public class AnyArrayMetaType extends YamlArrayType {

    private static final AnyArrayMetaType INSTANCE = new AnyArrayMetaType();

    public static AnyArrayMetaType getInstance() {
        return INSTANCE;
    }

    public AnyArrayMetaType() {
        super(AnythingMetaType.getInstance());
    }
}
