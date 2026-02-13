package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.StringMetaType;

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

    private ScriptOutParamsMetaType() {
        super(StringMetaType.getInstance(), AnyMapMetaType.getInstance());

        setDescriptionKey("doc.step.feature.out.description");
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
