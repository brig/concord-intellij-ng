package brig.concord.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public sealed interface SchemaType {

    record Scalar(@NotNull String typeName) implements SchemaType {}

    record Array(@Nullable String itemType) implements SchemaType {}

    record Enum(@NotNull List<String> values, @NotNull List<String> descriptions) implements SchemaType {
        public Enum(@NotNull List<String> values) {
            this(values, List.of());
        }
    }

    record Composite(@NotNull List<SchemaType> alternatives) implements SchemaType {}

    record Object(@NotNull ObjectSchema section) implements SchemaType {}

    record Any() implements SchemaType {}

    static @NotNull String displayName(@NotNull SchemaType schemaType) {
        return switch (schemaType) {
            case Scalar s -> s.typeName();
            case Array a -> {
                var itemType = a.itemType();
                yield (itemType != null ? itemType : "any") + "[]";
            }
            case Enum e -> "enum";
            case Composite c -> c.alternatives().stream()
                    .map(SchemaType::displayName)
                    .collect(Collectors.joining("|"));
            case Object o -> "object";
            case Any a -> "any";
        };
    }
}
