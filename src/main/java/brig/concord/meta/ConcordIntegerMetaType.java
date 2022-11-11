package brig.concord.meta;

import org.jetbrains.yaml.meta.model.YamlIntegerType;

public class ConcordIntegerMetaType extends YamlIntegerType {

    private static final ConcordIntegerMetaType INSTANCE = new ConcordIntegerMetaType();

    public static ConcordIntegerMetaType getInstance() {
        return INSTANCE;
    }

    public ConcordIntegerMetaType() {
        super(false);
    }
}
