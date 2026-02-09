package brig.concord.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public sealed interface SchemaType {

    record Scalar(@NotNull String typeName) implements SchemaType {}

    record Array(@Nullable String itemType) implements SchemaType {}

    record Enum(@NotNull List<String> values) implements SchemaType {}

    record Composite(@NotNull List<SchemaType> alternatives) implements SchemaType {}

    record Any() implements SchemaType {}
}
