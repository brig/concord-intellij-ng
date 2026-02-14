package brig.concord.schema;

import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.model.value.AnyOfType;
import brig.concord.meta.model.value.ExpressionMetaType;
import brig.concord.meta.model.value.AnythingMetaType;
import brig.concord.meta.model.value.ParamMetaTypes;
import brig.concord.yaml.meta.model.Field;
import brig.concord.yaml.meta.model.TypeProps;
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
    public @NotNull List<String> computeMissingFields(@NotNull Set<String> existingFields) {
        return section.requiredFields().stream()
                .filter(s -> !existingFields.contains(s))
                .sorted()
                .collect(Collectors.toList());
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
        var description = property.description();
        var props = (description != null || property.required())
                ? TypeProps.desc(description).andRequired(property.required())
                : null;
        return schemaTypeToMetaType(property.schemaType(), props);
    }

    private static YamlMetaType schemaTypeToMetaType(@NotNull SchemaType schemaType,
                                                     @Nullable TypeProps props) {
        return switch (schemaType) {
            case SchemaType.Scalar s -> switch (s.typeName()) {
                case "string" -> withProps(ParamMetaTypes.STRING_OR_EXPRESSION, props);
                case "boolean" -> withProps(ParamMetaTypes.BOOLEAN_OR_EXPRESSION, props);
                case "integer", "number" -> withProps(ParamMetaTypes.NUMBER_OR_EXPRESSION, props);
                case "object" -> withProps(ParamMetaTypes.OBJECT_OR_EXPRESSION, props);
                default -> props != null ? new AnythingMetaType(props) : AnythingMetaType.getInstance();
            };
            case SchemaType.Array a -> withProps(arrayMetaType(a.itemType()), props);
            case SchemaType.Enum e -> {
                var enumType = new YamlEnumType("string",
                        YamlEnumType.EnumValue.fromLiterals(e.values(), e.descriptions()));
                yield withProps(AnyOfType.anyOf(enumType, ExpressionMetaType.getInstance()), props);
            }
            case SchemaType.Composite c -> {
                var metaTypes = c.alternatives().stream()
                        .map(alt -> schemaTypeToMetaType(alt, null))
                        .toArray(YamlMetaType[]::new);
                yield withProps(AnyOfType.anyOf(metaTypes), props);
            }
            case SchemaType.Any a -> props != null ? new AnythingMetaType(props) : AnythingMetaType.getInstance();
        };
    }

    private static AnyOfType withProps(@NotNull AnyOfType base, @Nullable TypeProps props) {
        return props != null ? base.withProps(props) : base;
    }

    private static AnyOfType arrayMetaType(@Nullable String itemType) {
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
