// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SchemaProperty(
        @NotNull String name,
        @NotNull SchemaType schemaType,
        @Nullable String description,
        boolean required
) {

    public SchemaProperty withRequired(boolean required) {
        return new SchemaProperty(name, schemaType, description, required);
    }

    public static SchemaProperty any(String name) {
        return new SchemaProperty(name, new SchemaType.Any(), null, false);
    }
}
