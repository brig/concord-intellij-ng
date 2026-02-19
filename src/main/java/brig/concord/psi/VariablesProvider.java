package brig.concord.psi;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.StepElementMetaType;
import brig.concord.schema.*;
import brig.concord.yaml.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public final class VariablesProvider {

    private VariablesProvider() {}

    public static @NotNull List<Variable> getVariables(@NotNull PsiElement element) {
        Map<String, Variable> result = new LinkedHashMap<>();

        collectBuiltInVars(result);
        collectArguments(element, result);

        var flowKv = ProcessDefinition.findEnclosingFlowDefinition(element);
        if (flowKv != null) {
            collectFlowInputParams(flowKv, result);
            collectVariablesFromScope(element, result);
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
            result.put(name, new Variable(name, VariableSource.ARGUMENT, kv, SchemaInference.inferSchema(name, kv.getValue())));
        }
    }

    private static void collectFlowInputParams(YAMLKeyValue flowKv, Map<String, Variable> result) {
        var doc = FlowCallParamsProvider.findFlowDocumentationBefore(flowKv);
        if (doc == null) {
            return;
        }

        for (var param : doc.getInputParameters()) {
            var name = param.getName();
            var schemaProp = SchemaInference.fromFlowDocParameter(param);
            result.put(name, new Variable(name, VariableSource.FLOW_PARAMETER, param, schemaProp));
        }
    }

    private static void collectVariablesFromScope(PsiElement element, Map<String, Variable> result) {
        var metaProvider = ConcordMetaTypeProvider.getInstance(element.getProject());
        var current = element;
        while (true) {
            var sequenceItem = PsiTreeUtil.getParentOfType(current, YAMLSequenceItem.class);
            if (sequenceItem == null || !(sequenceItem.getParent() instanceof YAMLSequence sequence)) {
                break;
            }

            if (metaProvider.getResolvedMetaType(sequenceItem) instanceof StepElementMetaType) {
                collectLocalStepVariables(sequenceItem, element, result);
                collectPreviousSiblingsVariables(sequence, sequenceItem, result);
            }

            current = sequence;
        }
    }

    private static void collectLocalStepVariables(YAMLSequenceItem stepItem, PsiElement cursorElement, Map<String, Variable> result) {
        if (!(stepItem.getValue() instanceof YAMLMapping m)) {
            return;
        }

        var loopKv = m.getKeyValueByKey("loop");
        if (loopKv != null) {
            collectLoopVars(result, loopKv);
        }

        var outKv = m.getKeyValueByKey("out");
        var taskKv = m.getKeyValueByKey("task");
        if (taskKv != null && PsiTreeUtil.isAncestor(outKv, cursorElement, false)) {
            var taskOutType = resolveTaskOutType(taskKv);
            var schema = taskResultSchema("result", taskOutType);
            result.put("result", new Variable("result", VariableSource.TASK_RESULT, outKv, schema));
        }
    }

    private static void collectPreviousSiblingsVariables(YAMLSequence sequence, YAMLSequenceItem currentItem, Map<String, Variable> result) {
        for (var item : sequence.getItems()) {
            if (item == currentItem) {
                break;
            }
            if (item.getValue() instanceof YAMLMapping mapping) {
                collectFromStep(mapping, result);
            }
        }
    }

    private static void collectFromStep(YAMLMapping stepMapping, Map<String, Variable> result) {
        if (stepMapping.getKeyValueByKey("switch") != null) {
            stepMapping.getKeyValues().stream()
                    .filter(kv -> !"switch".equals(kv.getKeyText()))
                    .forEach(kv -> collectFromBranch(kv.getValue(), result));
            return;
        }

        var callKv = stepMapping.getKeyValueByKey("call");
        var taskKv = stepMapping.getKeyValueByKey("task");

        Function<String, SchemaProperty> outResolver = createOutResolver(callKv, taskKv);

        for (var kv : stepMapping.getKeyValues()) {
            var value = kv.getValue();
            switch (kv.getKeyText()) {
                case "set" -> collectSetVars(value, result);
                case "out" -> extractOutVars(value, result, outResolver);
                case "then", "else", "try", "error", "block" -> collectFromBranch(value, result);
            }
        }
    }

    private static @NotNull Function<String, SchemaProperty> createOutResolver(YAMLKeyValue callKv, YAMLKeyValue taskKv) {
        if (callKv != null) {
            var types = resolveCallOutTypes(callKv);
            return name -> types.getOrDefault(name, SchemaProperty.any(name));
        } else if (taskKv != null) {
            var taskProps = resolveTaskOutType(taskKv);
            return name -> taskResultSchema(name, taskProps);
        }

        return SchemaProperty::any;
    }

    private static void collectSetVars(YAMLValue value, Map<String, Variable> result) {
        if (!(value instanceof YAMLMapping m)) {
            return;
        }

        for (var entry : m.getKeyValues()) {
            var name = entry.getKeyText().trim();
            if (!name.isEmpty()) {
                result.put(name, new Variable(name, VariableSource.SET_STEP, entry, SchemaInference.inferSchema(name, entry.getValue())));
            }
        }
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
        if (doc == null || doc.getOutputParameters().isEmpty()) {
            return Map.of();
        }

        var types = new HashMap<String, SchemaProperty>();
        for (var param : doc.getOutputParameters()) {
            types.put(param.getName(), SchemaInference.fromFlowDocParameter(param));
        }
        return types;
    }

    private static @NotNull Map<String, SchemaProperty> resolveTaskOutType(@NotNull YAMLKeyValue taskKv) {
        var taskName = taskKv.getValueText();
        if (taskName.isBlank()) {
            return Map.of();
        }

        var schema = TaskSchemaRegistry.getInstance(taskKv.getProject()).getSchema(taskName);
        if (schema == null) return Map.of();

        return schema.outSection().properties();
    }

    private static SchemaProperty taskResultSchema(String name, Map<String, SchemaProperty> properties) {
        if (properties.isEmpty()) {
            return SchemaProperty.any(name);
        }
        return new SchemaProperty(name, new SchemaType.Object(new ObjectSchema(properties, Set.of(), false)), "task result", false);
    }

    private static void extractOutVars(YAMLValue value, Map<String, Variable> result, Function<String, SchemaProperty> schemaResolver) {
        if (value instanceof YAMLScalar s) {
            addOutVar(s.getTextValue(), s, result, schemaResolver);
        } else if (value instanceof YAMLSequence seq) {
            for (var item : seq.getItems()) {
                if (item.getValue() instanceof YAMLScalar s) {
                    addOutVar(s.getTextValue(), s, result, schemaResolver);
                }
            }
        } else if (value instanceof YAMLMapping m) {
            for (var kv : m.getKeyValues()) {
                addOutVar(kv.getKeyText(), kv, result, SchemaProperty::any);
            }
        }
    }

    private static void addOutVar(String name, PsiElement element, Map<String, Variable> result, Function<String, SchemaProperty> schemaResolver) {
        name = name.trim();
        if (!name.isEmpty()) {
            var schema = schemaResolver.apply(name);
            result.put(name, new Variable(name, VariableSource.STEP_OUT, element, schema));
        }
    }
}
