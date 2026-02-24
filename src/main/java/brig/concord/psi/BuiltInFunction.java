// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import brig.concord.schema.SchemaProperty;
import brig.concord.schema.SchemaType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public record BuiltInFunction(@NotNull String name,
                               @NotNull SchemaType returnType,
                               @NotNull String description,
                               @NotNull List<SchemaProperty> params) {

    public @NotNull String signature() {
        return params.stream()
                .map(p -> SchemaType.displayName(p.schemaType()) + " " + p.name())
                .collect(Collectors.joining(", ", "(", ")"));
    }
}