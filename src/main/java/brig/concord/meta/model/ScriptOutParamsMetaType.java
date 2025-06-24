package brig.concord.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;

import java.util.List;

public class ScriptOutParamsMetaType extends YamlAnyOfType {

    private static final ScriptOutParamsMetaType INSTANCE = new ScriptOutParamsMetaType();

    public static ScriptOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected ScriptOutParamsMetaType() {
        super("out params [object|string]", List.of(StringMetaType.getInstance(), AnyMapMetaType.getInstance()));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
