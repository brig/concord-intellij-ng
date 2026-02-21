// SPDX-License-Identifier: Apache-2.0
package brig.concord.schema;

import brig.concord.ConcordType;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.YamlPsiUtils;
import brig.concord.yaml.YAMLUtil;
import brig.concord.yaml.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;

public final class SchemaInference {

    private SchemaInference() {}

    public static @NotNull SchemaProperty fromFlowDocParameter(@NotNull FlowDocParameter param) {
        var schemaType = SchemaInference.fromFlowDocParameterType(param);
        return new SchemaProperty(param.getName(), schemaType, param.getDescription(), param.isMandatory());
    }

    public static @NotNull SchemaType fromFlowDocParameterType(@NotNull FlowDocParameter param) {
        var concordType = ConcordType.resolve(param.getBaseType(), ConcordType.YamlBaseType.ANY);
        if (param.isArrayType()) {
            return new SchemaType.Array(concordType);
        }
        if (concordType == ConcordType.WellKnown.ANY) {
            return new SchemaType.Any();
        }
        return new SchemaType.Scalar(concordType);
    }

    public static @NotNull SchemaProperty inferSchema(@NotNull String name, @Nullable YAMLValue value) {
        return new SchemaProperty(name, inferType(value), null, false);
    }

    public static @NotNull SchemaType inferType(@Nullable YAMLValue value) {
        return switch (value) {
            case YAMLScalar scalar -> inferScalarType(scalar);
            case YAMLMapping mapping -> inferMappingType(mapping);
            case YAMLSequence ignored -> SchemaType.Array.ANY;
            case null, default -> SchemaType.ANY;
        };
    }

    private static @NotNull SchemaType inferScalarType(@NotNull YAMLScalar scalar) {
        if (YamlPsiUtils.isDynamicExpression(scalar)) {
            return new SchemaType.Any();
        }

        if (scalar instanceof YAMLQuotedText) {
            return SchemaType.Scalar.STRING;
        }

        var text = scalar.getTextValue().trim();
        if (YAMLUtil.isBooleanValue(text)) {
            return SchemaType.Scalar.BOOLEAN;
        }

        if (YAMLUtil.isNumberValue(text)) {
            return SchemaType.Scalar.INTEGER;
        }

        return SchemaType.Scalar.STRING;
    }

    private static @NotNull SchemaType inferMappingType(@NotNull YAMLMapping mapping) {
        var props = new LinkedHashMap<String, SchemaProperty>();
        for (var child : mapping.getKeyValues()) {
            var childName = child.getKeyText().trim();
            if (!childName.isEmpty()) {
                props.put(childName, inferSchema(childName, child.getValue()));
            }
        }

        var objectSchema = new ObjectSchema(
                Collections.unmodifiableMap(props),
                Collections.emptySet(),
                true
        );
        return new SchemaType.Object(objectSchema);
    }
}
