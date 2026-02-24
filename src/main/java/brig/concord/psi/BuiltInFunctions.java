// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import brig.concord.ConcordBundle;
import brig.concord.schema.SchemaProperty;
import brig.concord.schema.SchemaType;
import brig.concord.schema.SchemaType.Scalar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BuiltInFunctions {

    private static final BuiltInFunctions INSTANCE = new BuiltInFunctions();

    private final Map<String, BuiltInFunction> functions;

    private BuiltInFunctions() {
        var map = new LinkedHashMap<String, BuiltInFunction>();

        register(map, "allVariables", "doc.builtin.function.allVariables.description", Scalar.OBJECT, List.of());
        register(map, "hasVariable", "doc.builtin.function.hasVariable.description", Scalar.BOOLEAN,
                List.of(param("variableName", Scalar.STRING, "doc.builtin.function.hasVariable.param.variable_name.description")));
        register(map, "hasNonNullVariable", "doc.builtin.function.hasNonNullVariable.description", Scalar.BOOLEAN,
                List.of(param("variableName", Scalar.STRING, "doc.builtin.function.hasNonNullVariable.param.variable_name.description")));
        register(map, "currentFlowName", "doc.builtin.function.currentFlowName.description", Scalar.STRING, List.of());
        register(map, "evalAsMap", "doc.builtin.function.evalAsMap.description", Scalar.OBJECT,
                List.of(param("value", SchemaType.ANY, "doc.builtin.function.evalAsMap.param.value.description")));
        register(map, "hasFlow", "doc.builtin.function.hasFlow.description", Scalar.BOOLEAN,
                List.of(param("name", Scalar.STRING, "doc.builtin.function.hasFlow.param.name.description")));
        register(map, "isDebug", "doc.builtin.function.isDebug.description", Scalar.BOOLEAN, List.of());
        register(map, "isDryRun", "doc.builtin.function.isDryRun.description", Scalar.BOOLEAN, List.of());
        register(map, "orDefault", "doc.builtin.function.orDefault.description", SchemaType.ANY,
                List.of(param("name", Scalar.STRING, "doc.builtin.function.orDefault.param.name.description"),
                        param("defaultValue", SchemaType.ANY, "doc.builtin.function.orDefault.param.defaultValue.description")));
        register(map, "throw", "doc.builtin.function.throw.description", SchemaType.ANY,
                List.of(param("message", Scalar.STRING, "doc.builtin.function.throw.param.message.description")));
        register(map, "uuid", "doc.builtin.function.uuid.description", Scalar.STRING, List.of());

        this.functions = Map.copyOf(map);
    }

    public static @NotNull BuiltInFunctions getInstance() {
        return INSTANCE;
    }

    public @NotNull Collection<BuiltInFunction> getAll() {
        return functions.values();
    }

    public @Nullable BuiltInFunction get(@NotNull String name) {
        return functions.get(name);
    }

    private static void register(Map<String, BuiltInFunction> map,
                                 String name,
                                 @PropertyKey(resourceBundle = ConcordBundle.BUNDLE) String descriptionKey,
                                 SchemaType returnType,
                                 List<SchemaProperty> params) {
        map.put(name, new BuiltInFunction(name, returnType, ConcordBundle.message(descriptionKey), params));
    }

    private static SchemaProperty param(String name, SchemaType type,
                                        @PropertyKey(resourceBundle = ConcordBundle.BUNDLE) String descriptionKey) {
        return new SchemaProperty(name, type, ConcordBundle.message(descriptionKey), true);
    }
}
