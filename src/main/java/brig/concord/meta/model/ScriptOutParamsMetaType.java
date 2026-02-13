package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.StringMetaType;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static brig.concord.yaml.meta.model.TypeProps.desc;

public class ScriptOutParamsMetaType extends YamlAnyOfType {

    private static final ScriptOutParamsMetaType INSTANCE = new ScriptOutParamsMetaType();

    public static ScriptOutParamsMetaType getInstance() {
        return INSTANCE;
    }

    private ScriptOutParamsMetaType() {
        super(List.of(StringMetaType.getInstance(), AnyMapMetaType.getInstance()),
                desc("doc.step.feature.out.description"));
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
