package brig.concord.meta.model.value;

import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlArrayType;
import org.jetbrains.annotations.NotNull;

public class BooleanArrayMetaType extends YamlArrayType {

    private static final BooleanArrayMetaType INSTANCE = new BooleanArrayMetaType();

    public static BooleanArrayMetaType getInstance() {
        return INSTANCE;
    }

    public BooleanArrayMetaType() {
        super(BooleanMetaType.getInstance());
    }

    public BooleanArrayMetaType(@NotNull TypeProps props) {
        super(BooleanMetaType.getInstance(), props);
    }
}
