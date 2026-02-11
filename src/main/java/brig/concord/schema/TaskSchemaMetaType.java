package brig.concord.schema;

import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.model.value.AnyOfType;
import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.meta.model.value.AnythingMetaType;
import brig.concord.meta.model.value.ParamMetaTypes;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLMapping;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TaskSchemaMetaType extends ConcordMetaType {

    private final TaskSchemaSection section;
    private final Set<String> discriminatorKeys;
    private volatile Map<String, YamlMetaType> features;

    public TaskSchemaMetaType(@NotNull TaskSchemaSection section,
                              @NotNull Set<String> discriminatorKeys) {
        this.section = section;
        this.discriminatorKeys = discriminatorKeys;
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        var f = this.features;
        if (f != null) {
            return f;
        }

        var result = new HashMap<String, YamlMetaType>();
        for (var entry : section.properties().entrySet()) {
            var prop = entry.getValue();
            var metaType = toMetaType(prop);
            result.put(entry.getKey(), metaType);
        }
        this.features = Map.copyOf(result);
        return this.features;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return section.requiredFields();
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        var field = super.findFeatureByName(name);
        if (field != null) {
            return field;
        }

        // If additional properties allowed, accept any key
        if (section.additionalProperties()) {
            return metaTypeToField(AnythingMetaType.getInstance(), name);
        }
        return null;
    }

    @Override
    public @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
        var allFeatures = getFeatures();
        if (allFeatures.isEmpty()) {
            return List.of();
        }

        // Partition: discriminator keys first, then the rest
        var discriminatorFields = allFeatures.keySet().stream()
                .filter(discriminatorKeys::contains)
                .sorted()
                .map(this::findFeatureByName)
                .filter(Objects::nonNull)
                .toList();

        var otherFields = allFeatures.keySet().stream()
                .filter(k -> !discriminatorKeys.contains(k))
                .sorted()
                .map(this::findFeatureByName)
                .filter(Objects::nonNull)
                .toList();

        return Stream.concat(discriminatorFields.stream(), otherFields.stream())
                .collect(Collectors.toList());
    }

    private static YamlMetaType toMetaType(@NotNull TaskSchemaProperty property) {
        return schemaTypeToMetaType(property.schemaType());
    }

    private static YamlMetaType schemaTypeToMetaType(@NotNull SchemaType schemaType) {
        return switch (schemaType) {
            case SchemaType.Scalar s -> switch (s.typeName()) {
                case "string" -> ParamMetaTypes.STRING_OR_EXPRESSION;
                case "boolean" -> ParamMetaTypes.BOOLEAN_OR_EXPRESSION;
                case "integer", "number" -> ParamMetaTypes.NUMBER_OR_EXPRESSION;
                case "object" -> ParamMetaTypes.OBJECT_OR_EXPRESSION;
                default -> AnythingMetaType.getInstance();
            };
            case SchemaType.Array a -> arrayMetaType(a.itemType());
            case SchemaType.Enum e -> {
                var enumType = new YamlEnumType("enum")
                        .withLiterals(e.values().toArray(new String[0]));
                yield AnyOfType.anyOf(enumType, ExpressionMetaType.getInstance());
            }
            case SchemaType.Composite c -> {
                var metaTypes = c.alternatives().stream()
                        .map(TaskSchemaMetaType::schemaTypeToMetaType)
                        .toArray(YamlMetaType[]::new);
                yield AnyOfType.anyOf(metaTypes);
            }
            case SchemaType.Any a -> AnythingMetaType.getInstance();
        };
    }

    private static YamlMetaType arrayMetaType(@Nullable String itemType) {
        if (itemType == null) {
            return ParamMetaTypes.ARRAY_OR_EXPRESSION;
        }
        return switch (itemType) {
            case "string" -> ParamMetaTypes.STRING_ARRAY_OR_EXPRESSION;
            case "boolean" -> ParamMetaTypes.BOOLEAN_ARRAY_OR_EXPRESSION;
            case "integer", "number" -> ParamMetaTypes.NUMBER_ARRAY_OR_EXPRESSION;
            case "object" -> ParamMetaTypes.OBJECT_ARRAY_OR_EXPRESSION;
            default -> ParamMetaTypes.ARRAY_OR_EXPRESSION;
        };
    }
}
