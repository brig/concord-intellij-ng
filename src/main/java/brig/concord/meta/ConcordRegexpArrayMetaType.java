package brig.concord.meta;

import org.jetbrains.yaml.meta.model.YamlArrayType;
import org.jetbrains.yaml.meta.model.YamlStringType;

public class ConcordRegexpArrayMetaType extends YamlArrayType {

    private static final ConcordRegexpArrayMetaType INSTANCE = new ConcordRegexpArrayMetaType();

    public static ConcordRegexpArrayMetaType getInstance() {
        return INSTANCE;
    }

    public ConcordRegexpArrayMetaType() {
        super(ConcordRegexpMetaType.getInstance());
    }
}
