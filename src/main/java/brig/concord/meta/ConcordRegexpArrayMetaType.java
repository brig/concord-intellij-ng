package brig.concord.meta;

import org.jetbrains.yaml.meta.model.YamlArrayType;

public class ConcordRegexpArrayMetaType extends YamlArrayType {

    private static final ConcordRegexpArrayMetaType INSTANCE = new ConcordRegexpArrayMetaType();

    public static ConcordRegexpArrayMetaType getInstance() {
        return INSTANCE;
    }

    public ConcordRegexpArrayMetaType() {
        super(ConcordRegexpMetaType.getInstance());
    }
}
