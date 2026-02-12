package brig.concord.meta.model.value;

import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlArrayType;

import java.util.List;

public class RegexpOrArrayMetaType extends YamlAnyOfType {

    private static final RegexpOrArrayMetaType INSTANCE = new RegexpOrArrayMetaType();

    public static RegexpOrArrayMetaType getInstance() {
        return INSTANCE;
    }

    public RegexpOrArrayMetaType() {
        super("regexp|string[]", List.of(RegexpMetaType.getInstance(), new YamlArrayType(RegexpMetaType.getInstance())));
    }
}
