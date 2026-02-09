package brig.concord.schema;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public record TaskSchemaSection(
        @NotNull Map<String, TaskSchemaProperty> properties,
        @NotNull Set<String> requiredFields,
        boolean additionalProperties
) {

    public static TaskSchemaSection empty() {
        return new TaskSchemaSection(Collections.emptyMap(), Collections.emptySet(), true);
    }

    public TaskSchemaSection merge(TaskSchemaSection other) {
        var mergedProps = new LinkedHashMap<>(this.properties);
        mergedProps.putAll(other.properties);

        var mergedRequired = new LinkedHashSet<>(this.requiredFields);
        mergedRequired.addAll(other.requiredFields);

        boolean mergedAdditional = this.additionalProperties && other.additionalProperties;

        // Update required flag on properties
        var finalProps = new LinkedHashMap<String, TaskSchemaProperty>();
        for (var entry : mergedProps.entrySet()) {
            var prop = entry.getValue();
            if (mergedRequired.contains(entry.getKey()) && !prop.required()) {
                finalProps.put(entry.getKey(), prop.withRequired(true));
            } else {
                finalProps.put(entry.getKey(), prop);
            }
        }

        return new TaskSchemaSection(
                Collections.unmodifiableMap(finalProps),
                Collections.unmodifiableSet(mergedRequired),
                mergedAdditional
        );
    }
}
