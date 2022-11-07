package brig.concord.meta.model;

import org.jetbrains.yaml.meta.model.YamlScalarType;

public class ExpressionMetaType extends YamlScalarType {

    private static final ExpressionMetaType INSTANCE = new ExpressionMetaType();

    protected ExpressionMetaType() {
        super("Expression");
        setDisplayName("expression");
    }

    public static ExpressionMetaType getInstance() {
        return INSTANCE;
    }
}
