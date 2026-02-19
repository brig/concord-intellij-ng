package brig.concord.schema;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service(Service.Level.PROJECT)
public final class TaskSchemaRegistry {

    private static final Logger LOG = Logger.getInstance(TaskSchemaRegistry.class);

    private volatile TaskSchemaProvider provider = new ResourceTaskSchemaProvider();
    private final ConcurrentHashMap<String, Optional<TaskSchema>> cache = new ConcurrentHashMap<>();
    private final SchemaParser parser = new SchemaParser();

    public static TaskSchemaRegistry getInstance(@NotNull Project project) {
        return project.getService(TaskSchemaRegistry.class);
    }

    public @Nullable TaskSchema getSchema(@NotNull String taskName) {
        return cache.computeIfAbsent(taskName, this::loadSchema).orElse(null);
    }

    private @NotNull Optional<TaskSchema> loadSchema(@NotNull String taskName) {
        try {
            var stream = provider.getSchemaStream(taskName);
            if (stream == null) {
                return Optional.empty();
            }
            try (stream) {
                return Optional.of(parser.parse(taskName, stream));
            }
        } catch (Exception e) {
            LOG.warn("Failed to load task schema for: " + taskName, e);
            return Optional.empty();
        }
    }

    @TestOnly
    public void setProvider(@NotNull TaskSchemaProvider provider) {
        this.provider = provider;
        clearCache();
    }

    @TestOnly
    public void clearCache() {
        cache.clear();
    }
}
