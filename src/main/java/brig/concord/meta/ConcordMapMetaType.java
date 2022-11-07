package brig.concord.meta;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlMetaType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class ConcordMapMetaType extends ConcordMetaType {

    protected ConcordMapMetaType(@NotNull String name) {
        super(name);
    }

    protected abstract YamlMetaType getMapEntryType(String name);

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return new Field(name, getMapEntryType(name));
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return new HashMap<>();
    }
}