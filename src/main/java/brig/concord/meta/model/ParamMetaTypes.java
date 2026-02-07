package brig.concord.meta.model;

import brig.concord.meta.model.call.*;

public final class ParamMetaTypes {

    private ParamMetaTypes() {}

    public static final AnyOfType STRING_OR_EXPRESSION = AnyOfType.anyOf(StringInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType BOOLEAN_OR_EXPRESSION = AnyOfType.anyOf(BooleanInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType NUMBER_OR_EXPRESSION = AnyOfType.anyOf(IntegerInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType OBJECT_OR_EXPRESSION = AnyOfType.anyOf(AnyMapInParamMetaType.getInstance(), ExpressionMetaType.getInstance());

    public static final AnyOfType STRING_ARRAY_OR_EXPRESSION = AnyOfType.anyOf(StringArrayInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType BOOLEAN_ARRAY_OR_EXPRESSION = AnyOfType.anyOf(BooleanArrayInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType OBJECT_ARRAY_OR_EXPRESSION = AnyOfType.anyOf(ObjectArrayInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType NUMBER_ARRAY_OR_EXPRESSION = AnyOfType.anyOf(IntegerArrayInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType ARRAY_OR_EXPRESSION = AnyOfType.anyOf(AnyArrayInParamMetaType.getInstance(), ExpressionMetaType.getInstance());
}
