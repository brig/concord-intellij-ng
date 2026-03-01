// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import brig.concord.schema.SchemaType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public record TaskMethod(
        @NotNull String name,
        @NotNull SchemaType returnType,
        @NotNull List<SchemaType> parameterTypes) {

    public @NotNull String signature() {
        return parameterTypes.stream()
                .map(SchemaType::displayName)
                .collect(Collectors.joining(", ", "(", ")"));
    }
}
