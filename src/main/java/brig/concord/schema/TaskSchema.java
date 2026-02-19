package brig.concord.schema;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public record TaskSchema(@NotNull String taskName, @Nullable String description, @NotNull ObjectSchema baseInSection,
                         @NotNull List<SchemaConditional> inConditionals, @NotNull ObjectSchema outSection) {

    public TaskSchema(@NotNull String taskName,
                      @Nullable String description,
                      @NotNull ObjectSchema baseInSection,
                      @NotNull List<SchemaConditional> inConditionals,
                      @NotNull ObjectSchema outSection) {
        this.taskName = taskName;
        this.description = description;
        this.baseInSection = baseInSection;
        this.inConditionals = List.copyOf(inConditionals);
        this.outSection = outSection;
    }

    @Override
    public @NotNull String taskName() {
        return taskName;
    }

    @Override
    public @NotNull ObjectSchema baseInSection() {
        return baseInSection;
    }

    @Override
    public @NotNull List<SchemaConditional> inConditionals() {
        return inConditionals;
    }

    @Override
    public @NotNull ObjectSchema outSection() {
        return outSection;
    }

    public @NotNull Set<String> getDiscriminatorKeys() {
        var keys = new LinkedHashSet<String>();
        for (var conditional : inConditionals) {
            keys.addAll(conditional.discriminators().keySet());
        }
        return Collections.unmodifiableSet(keys);
    }

    public @NotNull ObjectSchema resolveInSection(@NotNull Map<String, String> currentValues) {
        var result = baseInSection;
        for (var conditional : inConditionals) {
            var allMatch = true;
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
