package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlStringType;

public class DependencyMetaType extends YamlStringType {

    private static final DependencyMetaType INSTANCE = new DependencyMetaType();

    public static DependencyMetaType getInstance() {
        return INSTANCE;
    }
}
