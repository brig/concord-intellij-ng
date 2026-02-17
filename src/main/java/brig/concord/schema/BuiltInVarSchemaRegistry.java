package brig.concord.schema;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class BuiltInVarSchemaRegistry {

    private static final Logger LOG = Logger.getInstance(BuiltInVarSchemaRegistry.class);
    private static final String RESOURCE_PREFIX = "/builtInVars/";
    private static final String RESOURCE_SUFFIX = ".schema.json";

    private static final ConcurrentHashMap<String, Optional<TaskSchemaSection>> CACHE = new ConcurrentHashMap<>();
    private static final TaskSchemaParser PARSER = new TaskSchemaParser();

    private BuiltInVarSchemaRegistry() {
    }

    public static @Nullable TaskSchemaSection getSchema(@NotNull String varName) {
        return CACHE.computeIfAbsent(varName, BuiltInVarSchemaRegistry::loadSchema).orElse(null);
    }

    private static @NotNull Optional<TaskSchemaSection> loadSchema(@NotNull String varName) {
        String path = RESOURCE_PREFIX + varName + RESOURCE_SUFFIX;
        try (var stream = BuiltInVarSchemaRegistry.class.getResourceAsStream(path)) {
            return Optional.ofNullable(stream)
                    .map(PARSER::parseSection)
                    .filter(s -> !s.properties().isEmpty());
        } catch (Exception e) {
            LOG.warn("Failed to load built-in var schema for: " + varName, e);
            return Optional.empty();
        }
    }
}
