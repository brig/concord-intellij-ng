package brig.concord.meta.model.value;

import brig.concord.meta.model.call.AnyMapInParamMetaType;

public final class ParamMetaTypes {

    private ParamMetaTypes() {}

    public static final AnyOfType STRING_OR_EXPRESSION = AnyOfType.anyOf(StringMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType BOOLEAN_OR_EXPRESSION = AnyOfType.anyOf(BooleanMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType NUMBER_OR_EXPRESSION = AnyOfType.anyOf(IntegerMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType OBJECT_OR_EXPRESSION = AnyOfType.anyOf(AnyMapInParamMetaType.getInstance(), ExpressionMetaType.getInstance());

    public static final AnyOfType STRING_ARRAY_OR_EXPRESSION = AnyOfType.anyOf(StringArrayMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType BOOLEAN_ARRAY_OR_EXPRESSION = AnyOfType.anyOf(BooleanArrayMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType OBJECT_ARRAY_OR_EXPRESSION = AnyOfType.anyOf(ObjectArrayMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType NUMBER_ARRAY_OR_EXPRESSION = AnyOfType.anyOf(IntegerArrayMetaType.getInstance(), ExpressionMetaType.getInstance());
    public static final AnyOfType ARRAY_OR_EXPRESSION = AnyOfType.anyOf(AnyArrayMetaType.getInstance(), ExpressionMetaType.getInstance());
}
