package brig.concord.schema;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record SchemaConditional(
        @NotNull Map<String, List<String>> discriminators,
        @NotNull ObjectSchema thenSection
) {
}
