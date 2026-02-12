package brig.concord.meta.model;

import brig.concord.meta.model.value.AnyMapMetaType;
import brig.concord.meta.model.value.ExpressionMetaType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlAnyOfType;

import java.util.List;

public class InParamsMetaType extends YamlAnyOfType {

    private static final InParamsMetaType INSTANCE = new InParamsMetaType();

    public static InParamsMetaType getInstance() {
        return INSTANCE;
    }

    protected InParamsMetaType() {
        super(ExpressionMetaType.getInstance(), AnyMapMetaType.getInstance());

        setDescriptionKey("doc.step.feature.in.description");
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return AnyMapMetaType.getInstance().findFeatureByName(name);
    }
}
