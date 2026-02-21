// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.schema;

import brig.concord.ConcordType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public sealed interface SchemaType {

    Any ANY = new Any();

    record Scalar(@NotNull ConcordType concordType) implements SchemaType {
        public static final Scalar STRING = new Scalar(ConcordType.WellKnown.STRING);
        public static final Scalar BOOLEAN = new Scalar(ConcordType.WellKnown.BOOLEAN);
        public static final Scalar INTEGER = new Scalar(ConcordType.WellKnown.INTEGER);
        public static final Scalar OBJECT = new Scalar(ConcordType.WellKnown.OBJECT);

        public @NotNull String typeName() {
            return concordType.displayName();
        }
    }

    record Array(@NotNull ConcordType itemType) implements SchemaType {
        public static final Array ANY = new Array(ConcordType.WellKnown.ANY);
    }

    record Enum(@NotNull List<String> values, @NotNull List<String> descriptions) implements SchemaType {
        public Enum(@NotNull List<String> values) {
            this(values, List.of());
        }
    }

    record Composite(@NotNull List<SchemaType> alternatives) implements SchemaType {
    }

    record Object(@NotNull ObjectSchema section) implements SchemaType {
    }

    record Any() implements SchemaType {
    }

    static @NotNull String displayName(@NotNull SchemaType schemaType) {
        return switch (schemaType) {
            case Scalar s -> s.typeName();
            case Array a -> a.itemType().displayName() + "[]";
            case Enum e -> "enum";
            case Composite c -> c.alternatives().stream()
                    .map(SchemaType::displayName)
                    .collect(Collectors.joining("|"));
            case Object o -> "object";
            case Any a -> "any";
        };
    }
}
