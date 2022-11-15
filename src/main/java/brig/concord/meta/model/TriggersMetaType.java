package brig.concord.meta.model;

import brig.concord.meta.model.trigger.TriggerMetaType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.meta.model.YamlAnyScalarType;
import org.jetbrains.yaml.meta.model.YamlArrayType;
import org.jetbrains.yaml.meta.model.YamlMetaType;

public class TriggersMetaType extends YamlArrayType {

    private static final TriggersMetaType INSTANCE = new TriggersMetaType();

    public static TriggersMetaType getInstance() {
        return INSTANCE;
    }

    public TriggersMetaType() {
        super(YamlAnyScalarType.getInstance());
    }

    @Override
    public @NotNull YamlMetaType getElementType() {
        return TriggerMetaType.getInstance();
    }
}
