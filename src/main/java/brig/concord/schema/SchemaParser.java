// SPDX-License-Identifier: Apache-2.0
package brig.concord.schema;

import brig.concord.ConcordType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SchemaParser {

    public @NotNull TaskSchema parse(@NotNull String taskName, @NotNull InputStream json) {
        var root = JsonParser.parseReader(new InputStreamReader(json, StandardCharsets.UTF_8)).getAsJsonObject();
        var description = getString(root, "description");
        var inSection = parseInSection(root);
        var outSection = parseOutSection(root);
        return new TaskSchema(taskName, description, inSection.base, inSection.conditionals, outSection);
    }

    public @NotNull ObjectSchema parseObject(@NotNull InputStream json) {
        var root = JsonParser.parseReader(new InputStreamReader(json, StandardCharsets.UTF_8)).getAsJsonObject();
        return parseSectionObject(root, root);
    }

    private record InSectionResult(ObjectSchema base, List<SchemaConditional> conditionals) {
    }

    private @NotNull InSectionResult parseInSection(@NotNull JsonObject root) {
        var inObj = root.getAsJsonObject("in");
        if (inObj == null) {
            return new InSectionResult(ObjectSchema.empty(), Collections.emptyList());
        }

        // Parse base properties (top-level properties)
        var baseProperties = parseProperties(root, inObj);
        var baseRequired = parseRequiredArray(inObj);
        var additionalProps = getAdditionalProperties(inObj);

        // Parse allOf for conditionals
        var conditionals = new ArrayList<SchemaConditional>();
        var allOf = inObj.getAsJsonArray("allOf");
        if (allOf != null) {
            for (var element : allOf) {
                if (!element.isJsonObject()) {
                    continue;
                }
                var obj = element.getAsJsonObject();

                var ifBlock = obj.getAsJsonObject("if");
                var thenBlock = obj.get("then");
                if (ifBlock != null && thenBlock != null) {
                    var conditional = parseConditional(root, ifBlock, thenBlock);
                    if (conditional != null) {
                        conditionals.add(conditional);
                    }
                }
            }
        }

        // Mark required properties
        var finalProps = applyRequired(baseProperties, baseRequired);

        var base = new ObjectSchema(
                Collections.unmodifiableMap(finalProps),
                Collections.unmodifiableSet(baseRequired),
                additionalProps
        );

        return new InSectionResult(base, conditionals);
    }

    private @NotNull ObjectSchema parseOutSection(@NotNull JsonObject root) {
        var outObj = root.getAsJsonObject("out");
        if (outObj == null) {
            return ObjectSchema.empty();
        }

        var properties = parseProperties(root, outObj);
        var required = parseRequiredArray(outObj);
        var additionalProps = getAdditionalProperties(outObj);
        var finalProps = applyRequired(properties, required);

        return new ObjectSchema(
                Collections.unmodifiableMap(finalProps),
                Collections.unmodifiableSet(required),
                additionalProps
        );
    }

    private @Nullable SchemaConditional parseConditional(@NotNull JsonObject root,
                                                             @NotNull JsonObject ifBlock,
                                                             @NotNull JsonElement thenElement) {
        // Extract discriminator from if.properties
        var ifProps = ifBlock.getAsJsonObject("properties");
        if (ifProps == null || ifProps.entrySet().isEmpty()) {
            return null;
        }

        // Collect discriminator values from all properties in the if block
        var discriminators = new LinkedHashMap<String, List<String>>();
        for (var entry : ifProps.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            var propSchema = entry.getValue().getAsJsonObject();

            var values = new ArrayList<String>();
            if (propSchema.has("const")) {
                var constValue = asStringValue(propSchema.get("const"));
                if (constValue != null) {
                    values.add(constValue);
                }
            } else if (propSchema.has("enum")) {
                for (var v : propSchema.getAsJsonArray("enum")) {
                    var sv = asStringValue(v);
                    if (sv != null) {
                        values.add(sv);
                    }
                }
            }

            if (!values.isEmpty()) {
                discriminators.put(entry.getKey(), List.copyOf(values));
            }
        }

        if (discriminators.isEmpty()) {
            return null;
        }

        // Parse then section
        var thenSection = parseThenSection(root, thenElement);

        return new SchemaConditional(Collections.unmodifiableMap(discriminators), thenSection);
    }

    private @NotNull ObjectSchema parseThenSection(@NotNull JsonObject root,
                                                        @NotNull JsonElement thenElement) {
        var resolved = resolveRef(root, thenElement);
        if (!resolved.isJsonObject()) {
            return ObjectSchema.empty();
        }

        var thenObj = resolved.getAsJsonObject();

        // Handle allOf inside then (merges multiple schemas)
        var allOf = thenObj.getAsJsonArray("allOf");
        if (allOf != null) {
            return mergeAllOf(root, allOf, thenObj);
        }

        // Direct properties
        return parseSectionObject(root, thenObj);
    }

    private @NotNull ObjectSchema mergeAllOf(@NotNull JsonObject root,
                                                  @NotNull JsonArray allOf,
                                                  @NotNull JsonObject context) {
        return mergeAllOf(root, allOf, context, 0);
    }

    private @NotNull ObjectSchema mergeAllOf(@NotNull JsonObject root,
                                                  @NotNull JsonArray allOf,
                                                  @NotNull JsonObject context,
                                                  int depth) {
        var result = ObjectSchema.empty();

        for (var element : allOf) {
            var resolved = resolveRef(root, element);
            if (!resolved.isJsonObject()) {
                continue;
            }
            var obj = resolved.getAsJsonObject();

            // Skip anyOf (alternative required groups) and if/then blocks within allOf
            if (obj.has("anyOf") && !obj.has("properties") && !obj.has("allOf")) {
                continue;
            }
            if (obj.has("if") && obj.has("then")) {
                continue;
            }

            var section = parseSectionObject(root, obj, depth);
            result = result.merge(section);
        }

        // Also check for properties/required directly on the context
        if (context.has("properties") || context.has("required")) {
            var contextSection = parseSectionDirect(root, context, depth);
            result = result.merge(contextSection);
        }

        return result;
    }

    private @NotNull ObjectSchema parseSectionObject(@NotNull JsonObject root,
                                                          @NotNull JsonObject obj) {
        return parseSectionObject(root, obj, 0);
    }

    private @NotNull ObjectSchema parseSectionObject(@NotNull JsonObject root,
                                                          @NotNull JsonObject obj,
                                                          int depth) {
        // If this object has allOf, recursively merge
        var nestedAllOf = obj.getAsJsonArray("allOf");
        if (nestedAllOf != null) {
            var allOfResult = mergeAllOf(root, nestedAllOf, obj, depth);
            // Also merge direct properties/required from this object
            var directSection = parseSectionDirect(root, obj, depth);
            return allOfResult.merge(directSection);
        }

        return parseSectionDirect(root, obj, depth);
    }

    private @NotNull ObjectSchema parseSectionDirect(@NotNull JsonObject root,
                                                          @NotNull JsonObject obj,
                                                          int depth) {
        var properties = parseProperties(root, obj, depth);
        var required = parseRequiredArray(obj);
        var additionalProps = getAdditionalProperties(obj);
        var finalProps = applyRequired(properties, required);

        return new ObjectSchema(
                Collections.unmodifiableMap(finalProps),
                Collections.unmodifiableSet(required),
                additionalProps
        );
    }

    private @NotNull Map<String, SchemaProperty> parseProperties(@NotNull JsonObject root,
                                                                     @NotNull JsonObject obj) {
        return parseProperties(root, obj, 0);
    }

    private @NotNull Map<String, SchemaProperty> parseProperties(@NotNull JsonObject root,
                                                                     @NotNull JsonObject obj,
                                                                     int depth) {
        var propsObj = obj.getAsJsonObject("properties");
        if (propsObj == null) {
            return Collections.emptyMap();
        }

        var result = new LinkedHashMap<String, SchemaProperty>();
        for (var entry : propsObj.entrySet()) {
            var name = entry.getKey();
            var propElement = resolveRef(root, entry.getValue());
            if (!propElement.isJsonObject()) {
                continue;
            }
            var propObj = propElement.getAsJsonObject();
            result.put(name, parseProperty(root, name, propObj, depth));
        }
        return result;
    }

    private @NotNull SchemaProperty parseProperty(@NotNull JsonObject root,
                                                      @NotNull String name,
                                                      @NotNull JsonObject propObj,
                                                      int depth) {
        var description = getString(propObj, "description");
        var schemaType = parseSchemaType(root, propObj, depth);
        return new SchemaProperty(name, schemaType, description, false);
    }

    private @NotNull SchemaType parseSchemaType(@NotNull JsonObject root, @NotNull JsonObject obj, int depth) {
        if (depth >= MAX_PARSE_DEPTH) {
            return new SchemaType.Any();
        }

        // 1. Check oneOf for const-enum pattern: oneOf: [{const: "a", description: "..."}, ...]
        var constEnum = parseConstEnum(root, obj);
        if (constEnum != null) {
            return constEnum;
        }

        // 2. Handle oneOf / anyOf → Composite (or unwrap if single alternative)
        var compositeAlternatives = parseCompositeAlternatives(root, obj, "oneOf", depth);
        if (compositeAlternatives == null) {
            compositeAlternatives = parseCompositeAlternatives(root, obj, "anyOf", depth);
        }
        if (compositeAlternatives != null) {
            return compositeAlternatives.size() == 1
                    ? compositeAlternatives.getFirst()
                    : new SchemaType.Composite(compositeAlternatives);
        }

        // 3. Check enum
        if (obj.has("enum")) {
            var enumValues = new ArrayList<String>();
            for (var v : obj.getAsJsonArray("enum")) {
                var sv = asStringValue(v);
                if (sv != null) {
                    enumValues.add(sv);
                }
            }
            return new SchemaType.Enum(List.copyOf(enumValues));
        }

        // 4. Check type
        var type = getString(obj, "type");
        if (type == null) {
            return new SchemaType.Any();
        }

        if ("array".equals(type)) {
            return new SchemaType.Array(getArrayItemType(root, obj));
        }

        if ("object".equals(type) && (obj.has("properties") || obj.has("allOf"))) {
            return new SchemaType.Object(parseSectionObject(root, obj, depth + 1));
        }

        var concordType = ConcordType.fromString(type);
        if (concordType == null) {
            return new SchemaType.Any();
        }
        return new SchemaType.Scalar(concordType);
    }

    private @Nullable List<SchemaType> parseCompositeAlternatives(@NotNull JsonObject root,
                                                                  @NotNull JsonObject obj,
                                                                  @NotNull String keyword,
                                                                  int depth) {
        var arr = obj.getAsJsonArray(keyword);
        if (arr == null) {
            return null;
        }

        var alternatives = new ArrayList<SchemaType>();
        for (var element : arr) {
            var resolved = resolveRef(root, element);
            if (resolved.isJsonObject()) {
                alternatives.add(parseSchemaType(root, resolved.getAsJsonObject(), depth));
            }
        }
        return alternatives.isEmpty() ? null : List.copyOf(alternatives);
    }

    private @NotNull ConcordType getArrayItemType(@NotNull JsonObject root, @NotNull JsonObject obj) {
        var itemsElement = obj.get("items");
        if (itemsElement == null) {
            return ConcordType.WellKnown.ANY;
        }
        var resolved = resolveRef(root, itemsElement);
        if (resolved.isJsonObject()) {
            var typeName = getString(resolved.getAsJsonObject(), "type");
            if (typeName != null) {
                var concordType = ConcordType.fromString(typeName);
                if (concordType != null) {
                    return concordType;
                }
            }
        }
        return ConcordType.WellKnown.ANY;
    }

    private static final int MAX_PARSE_DEPTH = 10;
    private static final int MAX_REF_DEPTH = 10;

    private @NotNull JsonElement resolveRef(@NotNull JsonObject root, @NotNull JsonElement element) {
        return resolveRef(root, element, 0);
    }

    private @NotNull JsonElement resolveRef(@NotNull JsonObject root, @NotNull JsonElement element, int depth) {
        if (depth >= MAX_REF_DEPTH) {
            return element;
        }
        if (!element.isJsonObject()) {
            return element;
        }
        var obj = element.getAsJsonObject();
        var ref = getString(obj, "$ref");
        if (ref == null) {
            return element;
        }

        // Resolve $ref path like "#/definitions/startParams"
        if (!ref.startsWith("#/")) {
            return element;
        }

        var path = ref.substring(2).split("/");
        JsonElement current = root;
        for (var segment : path) {
            if (!current.isJsonObject()) {
                return element;
            }
            current = current.getAsJsonObject().get(segment);
            if (current == null) {
                return element;
            }
        }

        // Recursively resolve in case of nested $ref
        return resolveRef(root, current, depth + 1);
    }

    private static @NotNull Set<String> parseRequiredArray(@NotNull JsonObject obj) {
        var requiredArray = obj.getAsJsonArray("required");
        if (requiredArray == null) {
            return Collections.emptySet();
        }

        var result = new LinkedHashSet<String>();
        for (var element : requiredArray) {
            result.add(element.getAsString());
        }
        return result;
    }

    private static boolean getAdditionalProperties(@NotNull JsonObject obj) {
        var ap = obj.get("additionalProperties");
        if (ap != null && ap.isJsonPrimitive()) {
            return ap.getAsBoolean();
        }
        return true; // default to true
    }

    private @Nullable SchemaType.Enum parseConstEnum(@NotNull JsonObject root, @NotNull JsonObject obj) {
        var oneOf = obj.getAsJsonArray("oneOf");
        if (oneOf == null || oneOf.isEmpty()) {
            return null;
        }

        var values = new ArrayList<String>();
        var descriptions = new ArrayList<String>();
        var hasDescriptions = false;

        for (var element : oneOf) {
            var resolved = resolveRef(root, element);
            if (!resolved.isJsonObject()) {
                return null;
            }
            var item = resolved.getAsJsonObject();
            var constValue = item.get("const");
            if (constValue == null) {
                return null; // not a const-enum pattern
            }
            var sv = asStringValue(constValue);
            if (sv == null) {
                return null;
            }
            values.add(sv);
            var desc = getString(item, "description");
            descriptions.add(desc != null ? desc : "");
            if (desc != null) {
                hasDescriptions = true;
            }
        }

        if (values.isEmpty()) {
            return null;
        }

        return new SchemaType.Enum(
                List.copyOf(values),
                hasDescriptions ? List.copyOf(descriptions) : List.of()
        );
    }

    private static @Nullable String asStringValue(@NotNull JsonElement element) {
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return null;
    }

    private static @Nullable String getString(@NotNull JsonObject obj, @NotNull String key) {
        var element = obj.get(key);
        if (element != null && element.isJsonPrimitive()) {
            return element.getAsString();
        }
        return null;
    }

    private static @NotNull Map<String, SchemaProperty> applyRequired(
            @NotNull Map<String, SchemaProperty> properties,
            @NotNull Set<String> required) {
        if (required.isEmpty()) {
            return properties;
        }
        var result = new LinkedHashMap<String, SchemaProperty>();
        for (var entry : properties.entrySet()) {
            var prop = entry.getValue();
            if (required.contains(entry.getKey()) && !prop.required()) {
                result.put(entry.getKey(), prop.withRequired(true));
            } else {
                result.put(entry.getKey(), prop);
            }
        }
        return result;
    }
}
