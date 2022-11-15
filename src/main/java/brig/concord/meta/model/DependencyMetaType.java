package brig.concord.meta.model;

public class DependencyMetaType extends StringMetaType {

    private static final DependencyMetaType INSTANCE = new DependencyMetaType();

    public static DependencyMetaType getInstance() {
        return INSTANCE;
    }
}
