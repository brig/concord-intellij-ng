package brig.concord.schema;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TaskSchema {

    private final String taskName;
    private final TaskSchemaSection baseInSection;
    private final List<TaskSchemaConditional> inConditionals;
    private final TaskSchemaSection outSection;

    public TaskSchema(@NotNull String taskName,
                      @NotNull TaskSchemaSection baseInSection,
                      @NotNull List<TaskSchemaConditional> inConditionals,
                      @NotNull TaskSchemaSection outSection) {
        this.taskName = taskName;
        this.baseInSection = baseInSection;
        this.inConditionals = List.copyOf(inConditionals);
        this.outSection = outSection;
    }

    public @NotNull String getTaskName() {
        return taskName;
    }

    public @NotNull TaskSchemaSection getBaseInSection() {
        return baseInSection;
    }

    public @NotNull List<TaskSchemaConditional> getInConditionals() {
        return inConditionals;
    }

    public @NotNull TaskSchemaSection getOutSection() {
        return outSection;
    }

    public @NotNull Set<String> getDiscriminatorKeys() {
        var keys = new LinkedHashSet<String>();
        for (var conditional : inConditionals) {
            keys.addAll(conditional.discriminators().keySet());
        }
        return Collections.unmodifiableSet(keys);
    }

    public @NotNull TaskSchemaSection resolveInSection(@NotNull Map<String, String> currentValues) {
        var result = baseInSection;
        for (var conditional : inConditionals) {
            boolean allMatch = true;
            for (var entry : conditional.discriminators().entrySet()) {
                var currentValue = currentValues.get(entry.getKey());
                if (currentValue == null || !entry.getValue().contains(currentValue)) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                result = result.merge(conditional.thenSection());
            }
        }
        return result;
    }
}
