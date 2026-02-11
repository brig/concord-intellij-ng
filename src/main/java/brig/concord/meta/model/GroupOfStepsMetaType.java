package brig.concord.meta.model;

import java.util.Set;

public abstract class GroupOfStepsMetaType extends IdentityMetaType {

    public static final String ERROR = "error";

    protected GroupOfStepsMetaType(String name) {
        super(name, name, Set.of(name));
    }
}
