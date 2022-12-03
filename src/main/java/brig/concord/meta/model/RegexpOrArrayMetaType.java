package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlAnyOfType;
import org.jetbrains.yaml.meta.model.YamlArrayType;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class RegexpOrArrayMetaType extends YamlAnyOfType {

    private static final RegexpOrArrayMetaType INSTANCE = new RegexpOrArrayMetaType();

    public static RegexpOrArrayMetaType getInstance() {
        return INSTANCE;
    }

    protected RegexpOrArrayMetaType() {
        super("regexp|array", List.of(RegexpMetaType.getInstance(), new YamlArrayType(RegexpMetaType.getInstance())));
    }
}
