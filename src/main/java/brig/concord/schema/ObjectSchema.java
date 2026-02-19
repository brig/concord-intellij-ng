package brig.concord.schema;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public record ObjectSchema(
        @NotNull Map<String, SchemaProperty> properties,
        @NotNull Set<String> requiredFields,
        boolean additionalProperties
) {

    public static ObjectSchema empty() {
        return new ObjectSchema(Collections.emptyMap(), Collections.emptySet(), true);
    }

    public ObjectSchema merge(ObjectSchema other) {
        var mergedProps = new LinkedHashMap<>(this.properties);
        mergedProps.putAll(other.properties);

        var mergedRequired = new LinkedHashSet<>(this.requiredFields);
        mergedRequired.addAll(other.requiredFields);

        var mergedAdditional = this.additionalProperties && other.additionalProperties;

        // Update required flag on properties
        var finalProps = new LinkedHashMap<String, SchemaProperty>();
        for (var entry : mergedProps.entrySet()) {
            var prop = entry.getValue();
            if (mergedRequired.contains(entry.getKey()) && !prop.required()) {
                finalProps.put(entry.getKey(), prop.withRequired(true));
            } else {
                finalProps.put(entry.getKey(), prop);
            }
        }

        return new ObjectSchema(
                Collections.unmodifiableMap(finalProps),
                Collections.unmodifiableSet(mergedRequired),
                mergedAdditional
        );
    }
}
