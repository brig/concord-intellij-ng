package brig.concord.meta;

import org.jetbrains.yaml.meta.model.YamlAnything;
import org.jetbrains.yaml.meta.model.YamlMetaType;

public class ConcordAnyMapMetaType extends ConcordMapMetaType {

    private static final ConcordAnyMapMetaType INSTANCE = new ConcordAnyMapMetaType();

    public static ConcordAnyMapMetaType getInstance() {
        return INSTANCE;
    }

    protected ConcordAnyMapMetaType() {
        super("Object");
    }

    @Override
    protected YamlMetaType getMapEntryType(String name) {
        return YamlAnything.getInstance();
    }
}
