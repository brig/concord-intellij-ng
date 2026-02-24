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

        register(map, "allVariables", Scalar.OBJECT, List.of());
        register(map, "hasVariable", Scalar.BOOLEAN,
                List.of(param("variableName", Scalar.STRING, "doc.builtin.function.hasVariable.param.variable_name.description")));
        register(map, "hasNonNullVariable", Scalar.BOOLEAN,
                List.of(param("variableName", Scalar.STRING, "doc.builtin.function.hasNonNullVariable.param.variable_name.description")));
        register(map, "currentFlowName", Scalar.STRING, List.of());
        register(map, "evalAsMap", Scalar.OBJECT,
                List.of(param("value", SchemaType.ANY, "doc.builtin.function.evalAsMap.param.value.description")));
        register(map, "hasFlow", Scalar.BOOLEAN,
                List.of(param("name", Scalar.STRING, "doc.builtin.function.hasFlow.param.name.description")));
        register(map, "isDebug", Scalar.BOOLEAN, List.of());
        register(map, "isDryRun", Scalar.BOOLEAN, List.of());
        register(map, "orDefault", SchemaType.ANY,
                List.of(param("name", Scalar.STRING, "doc.builtin.function.orDefault.param.name.description"),
                        param("defaultValue", SchemaType.ANY, "doc.builtin.function.orDefault.param.defaultValue.description")));
        register(map, "throw", SchemaType.ANY,
                List.of(param("message", Scalar.STRING, "doc.builtin.function.throw.param.message.description")));
        register(map, "uuid", Scalar.STRING, List.of());

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
                                 String name, SchemaType returnType,
                                 List<SchemaProperty> params) {
        var description = ConcordBundle.message("doc.builtin.function." + name + ".description");
        map.put(name, new BuiltInFunction(name, returnType, description, params));
    }

    private static SchemaProperty param(String name, SchemaType type,
                                        @PropertyKey(resourceBundle = ConcordBundle.BUNDLE) String descriptionKey) {
        return new SchemaProperty(name, type, ConcordBundle.message(descriptionKey), true);
    }
}
