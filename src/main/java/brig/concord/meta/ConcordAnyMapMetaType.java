package brig.concord.meta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
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
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return YamlAnything.getInstance().findFeatureByName(name);
    }

    @Override
    protected YamlMetaType getMapEntryType(String name) {
        return YamlAnything.getInstance();
    }
}
