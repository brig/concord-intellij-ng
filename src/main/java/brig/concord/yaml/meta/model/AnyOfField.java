// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLValue;

public class AnyOfField extends Field {

    private final YamlAnyOfType anyOfType;

    public AnyOfField(@NotNull String name, @NotNull YamlAnyOfType anyOfType) {
        super(name, anyOfType);
        this.anyOfType = anyOfType;
    }

    @Override
    public @NotNull Field resolveToSpecializedField(@NotNull YAMLValue element) {
        var result = new Field(getName(), anyOfType);
        var arrayType = findArrayType(anyOfType);
        if (arrayType != null) {
            result = result.withRelationSpecificType(Relation.SEQUENCE_ITEM, arrayType.getElementType());
        }
        var scalar = findScalarType(element, anyOfType);
        if (scalar != null) {
            result = result.withRelationSpecificType(Relation.SCALAR_VALUE, scalar);
        }
        return result;
    }

    public static @Nullable YamlArrayType findArrayType(@NotNull YamlAnyOfType anyType) {
        for (var t : anyType.getSubTypes()) {
            if (t instanceof YamlArrayType) {
                return (YamlArrayType) t;
            }
        }
        return null;
    }

    private static @Nullable YamlMetaType findScalarType(@NotNull YAMLValue element, @NotNull YamlAnyOfType anyType) {
        YamlMetaType def = null;
        for (var t : anyType.getSubTypes()) {
            if (t instanceof YamlIntegerType) {
                if (element.getText().matches("[0-9]+")) {
                    return t;
                }
            } else if (t instanceof YamlScalarType) {
                def = t;
            }
        }
        return def;
    }
}
