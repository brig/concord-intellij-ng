package brig.concord.psi;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.StepElementMetaType;
import brig.concord.schema.*;
import brig.concord.yaml.psi.*;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public final class VariablesProvider {

    private static final Key<CachedValue<List<Variable>>> BASE_VARS_KEY =
            Key.create("concord.variables.base");

    private VariablesProvider() {}

    public static @NotNull List<Variable> getVariables(@NotNull PsiElement element) {
        var sequenceItem = PsiTreeUtil.getParentOfType(element, YAMLSequenceItem.class);
        if (sequenceItem == null) {
            return computeWithoutScope(element);
        }

        var baseVars = getCachedBaseVariables(sequenceItem);

        var resultVar = resolveTaskResult(sequenceItem, element);
        if (resultVar == null) {
            return baseVars;
        }

        var withResult = new ArrayList<Variable>(baseVars.size() + 1);
        withResult.addAll(baseVars);
        withResult.add(resultVar);
        return Collections.unmodifiableList(withResult);
    }

    private static @NotNull List<Variable> getCachedBaseVariables(@NotNull YAMLSequenceItem stepItem) {
        return CachedValuesManager.getCachedValue(stepItem, BASE_VARS_KEY, () -> {
            Map<String, Variable> result = new LinkedHashMap<>();
            collectBuiltInVars(result);
            collectArguments(stepItem, result);

            var flowKv = ProcessDefinition.findEnclosingFlowDefinition(stepItem);
            if (flowKv != null) {
                collectFlowInputParams(flowKv, result);
                collectVariablesFromScopeBase(stepItem, result);
            }

            return CachedValueProvider.Result.create(
                    List.copyOf(result.values()),
                    PsiModificationTracker.getInstance(stepItem.getProject())
            );
        });
    }

    private static @NotNull List<Variable> computeWithoutScope(@NotNull PsiElement element) {
        Map<String, Variable> result = new LinkedHashMap<>();
        collectBuiltInVars(result);
        collectArguments(element, result);
        var flowKv = ProcessDefinition.findEnclosingFlowDefinition(element);
        if (flowKv != null) {
            collectFlowInputParams(flowKv, result);
        }
        return List.copyOf(result.values());
    }

    private static @Nullable Variable resolveTaskResult(YAMLSequenceItem stepItem, PsiElement cursor) {
        if (!(stepItem.getValue() instanceof YAMLMapping m)) {
            return null;
        }
        var taskKv = m.getKeyValueByKey("task");
        var outKv = m.getKeyValueByKey("out");
        if (taskKv == null || !PsiTreeUtil.isAncestor(outKv, cursor, false)) {
            return null;
        }
        var schema = taskResultSchema("result", resolveTaskOutType(taskKv));
        return new Variable("result", VariableSource.TASK_RESULT, outKv, schema);
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

    private static void collectVariablesFromScopeBase(YAMLSequenceItem startItem, Map<String, Variable> result) {
        var metaProvider = ConcordMetaTypeProvider.getInstance(startItem.getProject());
        var sequenceItem = startItem;
        var current = (PsiElement) startItem;

        while (true) {
            if (sequenceItem == null || !(sequenceItem.getParent() instanceof YAMLSequence sequence)) {
                break;
            }

            if (metaProvider.getResolvedMetaType(sequenceItem) instanceof StepElementMetaType) {
                collectLocalStepVarsBase(sequenceItem, result);
                collectPreviousSiblingsVariables(sequence, sequenceItem, result);
            }

            current = sequence;
            sequenceItem = PsiTreeUtil.getParentOfType(current, YAMLSequenceItem.class);
        }
    }

    private static void collectLocalStepVarsBase(YAMLSequenceItem stepItem, Map<String, Variable> result) {
        if (!(stepItem.getValue() instanceof YAMLMapping m)) {
            return;
        }
        var loopKv = m.getKeyValueByKey("loop");
        if (loopKv != null) {
            collectLoopVars(result, loopKv);
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
            return name -> taskResultSchema(name, resolveTaskOutType(taskKv));
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

    private static @Nullable ObjectSchema resolveTaskOutType(@NotNull YAMLKeyValue taskKv) {
        var taskName = taskKv.getValueText();
        if (taskName.isBlank()) {
            return null;
        }

        var schema = TaskSchemaRegistry.getInstance(taskKv.getProject()).getSchema(taskName);
        if (schema == null) {
            return null;
        }

        return schema.outSection();
    }

    private static @NotNull SchemaProperty taskResultSchema(String name, @Nullable ObjectSchema properties) {
        if (properties == null) {
            return SchemaProperty.any(name);
        }
        return new SchemaProperty(name, new SchemaType.Object(properties), "task result", false);
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
