package brig.concord.meta.model;

import brig.concord.yaml.meta.model.YamlArrayType;

public class DependenciesMetaType extends YamlArrayType {

    private static final DependenciesMetaType INSTANCE = new DependenciesMetaType();

    public static DependenciesMetaType getInstance() {
        return INSTANCE;
    }

    DependenciesMetaType() {
        super(DependencyMetaType.getInstance());
    }
}
