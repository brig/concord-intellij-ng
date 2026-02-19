package brig.concord.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class BuiltInVarsSchema {

    private static final BuiltInVarsSchema INSTANCE = new BuiltInVarsSchema();

    private final ObjectSchema builtInVars;
    private final ObjectSchema loopVars;

    private BuiltInVarsSchema() {
        var parser = new SchemaParser();
        builtInVars = loadSchema(parser, "/schema/builtInVars.schema.json");
        loopVars = loadSchema(parser, "/schema/loopVars.schema.json");
    }

    public static @NotNull BuiltInVarsSchema getInstance() {
        return INSTANCE;
    }

    public @NotNull ObjectSchema getBuiltInVars() {
        return builtInVars;
    }

    public @NotNull ObjectSchema getLoopVars() {
        return loopVars;
    }

    private static @NotNull ObjectSchema loadSchema(@NotNull SchemaParser parser, @NotNull String resourcePath) {
        try (var is = BuiltInVarsSchema.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + resourcePath);
            }
            return parser.parseObject(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
