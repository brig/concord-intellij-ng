package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlScalarType;

public class DependencyMetaType extends YamlScalarType {

    private static final DependencyMetaType INSTANCE = new DependencyMetaType();

    public static DependencyMetaType getInstance() {
        return INSTANCE;
    }

    private DependencyMetaType() {
        super("Dependency");
    }
}
