package brig.concord.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record TaskSchemaProperty(
        @NotNull String name,
        @NotNull SchemaType schemaType,
        @Nullable String description,
        boolean required
) {

    public TaskSchemaProperty withRequired(boolean required) {
        return new TaskSchemaProperty(name, schemaType, description, required);
    }
}
