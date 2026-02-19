package brig.concord.psi;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.StepElementMetaType;
import brig.concord.schema.BuiltInVarsSchema;
import brig.concord.schema.ObjectSchema;
import brig.concord.schema.SchemaProperty;
import brig.concord.schema.SchemaType;
import brig.concord.yaml.YAMLUtil;
import brig.concord.yaml.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class VariablesProvider {

    private static final String TASK_RESULT_VAR = "result";

    public record Variable(@NotNull String name, @NotNull VariableSource source,
                           @Nullable PsiElement declaration, @Nullable SchemaProperty schema) {}

    public enum VariableSource {
        BUILT_IN("built-in", "built-in variable"),
        ARGUMENT("argument", "process argument"),
        FLOW_PARAMETER("flow in", "flow input parameter"),
        SET_STEP("set", "set step variable"),
        STEP_OUT("step out", "step output variable"),
        LOOP("loop", "loop variable"),
        TASK_RESULT("task result", "task result variable");

        private final String shortLabel;
        private final String description;

        VariableSource(String shortLabel, String description) {
            this.shortLabel = shortLabel;
            this.description = description;
        }

        public @NotNull String shortLabel() {
            return shortLabel;
        }

        public @NotNull String description() {
            return description;
        }
    }

    private VariablesProvider() {}

    public static @NotNull List<Variable> getVariables(@NotNull PsiElement element) {
        Map<String, Variable> result = new LinkedHashMap<>();

        collectBuiltInVars(result);
        collectArguments(element, result);

        var flowKv = ProcessDefinition.findEnclosingFlowDefinition(element);
        if (flowKv != null) {
            collectFlowDocParams(flowKv, result);
            collectFromSteps(element, result);
        }

        return new ArrayList<>(result.values());
    }

    private static void collectBuiltInVars(Map<String, Variable> result) {
        var schema = BuiltInVarsSchema.getInstance();
        for (var entry : schema.getBuiltInVars().properties().entrySet()) {
            result.put(entry.getKey(), new Variable(entry.getKey(), VariableSource.BUILT_IN, null, entry.getValue()));
        }
    }

    private static void collectLoopVars(Map<String, Variable> result, PsiElement declaration) {
        var schema = BuiltInVarsSchema.getInstance();
        for (var entry : schema.getLoopVars().properties().entrySet()) {
            result.put(entry.getKey(), new Variable(entry.getKey(), VariableSource.LOOP, declaration, entry.getValue()));
        }
    }

    private static void collectArguments(PsiElement context, Map<String, Variable> result) {
        var args = ArgumentsCollector.getInstance(context.getProject()).getArguments(context);
        for (var entry : args.entrySet()) {
            var name = entry.getKey();
            var kv = entry.getValue();
            result.put(name, new Variable(name, VariableSource.ARGUMENT, kv, inferSchema(name, kv.getValue())));
        }
    }

    private static void collectFlowDocParams(YAMLKeyValue flowKv, Map<String, Variable> result) {
        var doc = FlowCallParamsProvider.findFlowDocumentationBefore(flowKv);
        if (doc == null) {
            return;
        }

        for (var param : doc.getInputParameters()) {
            var name = param.getName();
            var schemaType = flowDocTypeToSchemaType(param);
            var schemaProp = new SchemaProperty(name, schemaType, param.getDescription(), param.isMandatory());
            result.put(name, new Variable(name, VariableSource.FLOW_PARAMETER, param, schemaProp));
        }
    }

    private static @NotNull SchemaType flowDocTypeToSchemaType(@NotNull FlowDocParameter param) {
        if (param.isArrayType()) {
            return new SchemaType.Array(param.getBaseType());
        }
        if (param.getType().equals("any")) {
            return new SchemaType.Any();
        }
        return new SchemaType.Scalar(param.getType());
    }

    private static void collectFromSteps(PsiElement element, Map<String, Variable> result) {
        var metaProvider = ConcordMetaTypeProvider.getInstance(element.getProject());
        var current = element;
        while (true) {
            var sequenceItem = PsiTreeUtil.getParentOfType(current, YAMLSequenceItem.class);
            if (sequenceItem == null) {
                break;
            }

            var parent = sequenceItem.getParent();
            if (!(parent instanceof YAMLSequence sequence)) {
                break;
            }

            var metaType = metaProvider.getResolvedMetaType(sequenceItem);
            if (metaType instanceof StepElementMetaType) {
                if (sequenceItem.getValue() instanceof YAMLMapping m) {
                    var loopKv = m.getKeyValueByKey("loop");
                    if (loopKv != null) {
                        collectLoopVars(result, loopKv);
                    }
                    var outKv = m.getKeyValueByKey("out");
                    if (m.getKeyValueByKey("task") != null
                            && PsiTreeUtil.isAncestor(outKv, element, false)) {
                        result.put(TASK_RESULT_VAR, new Variable(TASK_RESULT_VAR, VariableSource.TASK_RESULT, outKv, null));
                    }
                }
                for (var item : sequence.getItems()) {
                    if (item == sequenceItem) {
                        break;
                    }
                    var value = item.getValue();
                    if (value instanceof YAMLMapping mapping) {
                        collectFromStep(mapping, result);
                    }
                }
            }

            var seqParent = sequence.getParent();
            if (seqParent instanceof YAMLKeyValue kv && ProcessDefinition.isFlowDefinition(kv)) {
                break;
            }

            current = sequence;
        }
    }

    private static void collectFromStep(YAMLMapping stepMapping, Map<String, Variable> result) {
        if (stepMapping.getKeyValueByKey("switch") != null) {
            collectFromSwitchStep(stepMapping, result);
            return;
        }

        var callKv = stepMapping.getKeyValueByKey("call");

        for (var kv : stepMapping.getKeyValues()) {
            switch (kv.getKeyText()) {
                case "set" -> collectSetVars(kv.getValue(), result);
                case "out" -> extractOutVars(kv.getValue(), result,
                        callKv != null ? resolveCallOutTypes(callKv) : Map.of());
                case "then", "else", "try", "error", "block" -> collectFromBranch(kv.getValue(), result);
            }
        }
    }

    private static void collectFromSwitchStep(YAMLMapping stepMapping, Map<String, Variable> result) {
        for (var kv : stepMapping.getKeyValues()) {
            if (!"switch".equals(kv.getKeyText())) {
                collectFromBranch(kv.getValue(), result);
            }
        }
    }

    private static void collectSetVars(YAMLValue value, Map<String, Variable> result) {
        if (!(value instanceof YAMLMapping m)) {
            return;
        }

        for (var entry : m.getKeyValues()) {
            var name = entry.getKeyText().trim();
            if (!name.isEmpty()) {
                result.put(name, new Variable(name, VariableSource.SET_STEP, entry, inferSchema(name, entry.getValue())));
            }
        }
    }

    static @NotNull SchemaProperty inferSchema(@NotNull String name, @Nullable YAMLValue value) {
        return new SchemaProperty(name, inferType(value), null, false);
    }

    private static @NotNull SchemaType inferType(@Nullable YAMLValue value) {
        if (value == null) {
            return new SchemaType.Any();
        }

        if (value instanceof YAMLScalar scalar) {
            if (YamlPsiUtils.isDynamicExpression(scalar)) {
                return new SchemaType.Any();
            }
            if (value instanceof YAMLQuotedText) {
                return new SchemaType.Scalar("string");
            }
            var text = scalar.getTextValue().trim();
            if (YAMLUtil.isBooleanValue(text)) {
                return new SchemaType.Scalar("boolean");
            }
            if (YAMLUtil.isNumberValue(text)) {
                return new SchemaType.Scalar("integer");
            }
            return new SchemaType.Scalar("string");
        }

        if (value instanceof YAMLMapping mapping) {
            var props = new LinkedHashMap<String, SchemaProperty>();
            for (var child : mapping.getKeyValues()) {
                var childName = child.getKeyText().trim();
                if (!childName.isEmpty()) {
                    props.put(childName, inferSchema(childName, child.getValue()));
                }
            }
            var objectSchema = new ObjectSchema(
                    Collections.unmodifiableMap(props),
                    Collections.emptySet(),
                    true
            );
            return new SchemaType.Object(objectSchema);
        }

        if (value instanceof YAMLSequence) {
            return new SchemaType.Array(null);
        }

        return new SchemaType.Any();
    }

    private static void collectFromBranch(YAMLValue branchValue, Map<String, Variable> result) {
        if (!(branchValue instanceof YAMLSequence sequence)) {
            return;
        }
        for (var item : sequence.getItems()) {
            var value = item.getValue();
            if (value instanceof YAMLMapping mapping) {
                collectFromStep(mapping, result);
            }
        }
    }

    private static @NotNull Map<String, SchemaProperty> resolveCallOutTypes(@NotNull YAMLKeyValue callKv) {
        var doc = FlowCallParamsProvider.findFlowDocumentation(callKv);
        if (doc == null) {
            return Map.of();
        }

        var outParams = doc.getOutputParameters();
        if (outParams.isEmpty()) {
            return Map.of();
        }

        var types = new HashMap<String, SchemaProperty>();
        for (var param : outParams) {
            var name = param.getName();
            var schemaType = flowDocTypeToSchemaType(param);
            types.put(name, new SchemaProperty(name, schemaType, param.getDescription(), param.isMandatory()));
        }
        return types;
    }

    private static void extractOutVars(YAMLValue value, Map<String, Variable> result,
                                       @NotNull Map<String, SchemaProperty> outParamTypes) {
        if (value instanceof YAMLScalar s) {
            var name = s.getTextValue().trim();
            if (!name.isEmpty()) {
                result.put(name, new Variable(name, VariableSource.STEP_OUT, s, outParamTypes.get(name)));
            }
        } else if (value instanceof YAMLSequence seq) {
            for (var item : seq.getItems()) {
                var itemValue = item.getValue();
                if (itemValue instanceof YAMLScalar s) {
                    var name = s.getTextValue().trim();
                    if (!name.isEmpty()) {
                        result.put(name, new Variable(name, VariableSource.STEP_OUT, s, outParamTypes.get(name)));
                    }
                }
            }
        } else if (value instanceof YAMLMapping m) {
            for (var kv : m.getKeyValues()) {
                var name = kv.getKeyText().trim();
                if (!name.isEmpty()) {
                    result.put(name, new Variable(name, VariableSource.STEP_OUT, kv, SchemaProperty.any(name, null, false)));
                }
            }
        }
    }
}
