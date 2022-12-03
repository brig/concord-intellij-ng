package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlIntegerType;

@SuppressWarnings("UnstableApiUsage")
public class IntegerMetaType extends YamlIntegerType {

    private static final IntegerMetaType INSTANCE = new IntegerMetaType();

    public static IntegerMetaType getInstance() {
        return INSTANCE;
    }

    public IntegerMetaType() {
        super(false);
    }
}
