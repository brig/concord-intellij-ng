package brig.concord.meta;

import org.jetbrains.yaml.meta.model.YamlArrayType;
import org.jetbrains.yaml.meta.model.YamlStringType;

public class ConcordStringArrayMetaType extends YamlArrayType {

    private static final ConcordStringArrayMetaType INSTANCE = new ConcordStringArrayMetaType();

    public static ConcordStringArrayMetaType getInstance() {
        return INSTANCE;
    }

    public ConcordStringArrayMetaType() {
        super(YamlStringType.getInstance());
    }
}
