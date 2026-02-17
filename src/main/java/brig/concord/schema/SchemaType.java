// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface SchemaType {

    record Scalar(@NotNull String typeName) implements SchemaType {}

    record Array(@Nullable String itemType) implements SchemaType {}

    record Enum(@NotNull List<String> values, @NotNull List<String> descriptions) implements SchemaType {
        public Enum(@NotNull List<String> values) {
            this(values, List.of());
        }
    }

    record Composite(@NotNull List<SchemaType> alternatives) implements SchemaType {}

    record Object(@NotNull TaskSchemaSection section) implements SchemaType {}

    record Any() implements SchemaType {}
}
