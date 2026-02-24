// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.StepElementMetaType;
import brig.concord.schema.*;
import brig.concord.yaml.psi.*;
import com.intellij.openapi.project.Project;
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

    private static final Key<CachedValue<List<Variable>>> ARG_VARS_KEY =
            Key.create("concord.variables.arguments");

    private VariablesProvider() {
    }

    public static @NotNull List<Variable> getVariables(@NotNull PsiElement element) {
        var stepItem = findEnclosingStep(element);
        if (stepItem == null) {
            return computeWithoutScope(element);
        }

        var baseVars = getCachedBaseVariables(stepItem);
        var resultVar = resolveTaskResult(stepItem, element);

        if (resultVar == null) {
            return baseVars;
        }

        List<Variable> withResult = new ArrayList<>(baseVars.size() + 1);
        withResult.addAll(baseVars);
        withResult.add(resultVar);
        return Collections.unmodifiableList(withResult);
    }

    private static @NotNull List<Variable> getCachedBaseVariables(@NotNull YAMLSequenceItem stepItem) {
        return CachedValuesManager.getCachedValue(stepItem, BASE_VARS_KEY, () -> {
            var collector = new VariableCollector(stepItem.getProject());
            var flowKv = ProcessDefinition.findEnclosingFlowDefinition(stepItem);

            collectCommonVariables(stepItem, flowKv, collector);

            if (flowKv != null) {
                collectVariablesFromScope(stepItem, collector);
            }

            return CachedValueProvider.Result.create(
                    collector.toList(),
                    PsiModificationTracker.getInstance(stepItem.getProject())
            );
        });
    }

    private static @NotNull List<Variable> computeWithoutScope(@NotNull PsiElement element) {
        var collector = new VariableCollector(element.getProject());
        var flowKv = ProcessDefinition.findEnclosingFlowDefinition(element);
        collectCommonVariables(element, flowKv, collector);
        return collector.toList();
    }

    private static void collectCommonVariables(@NotNull PsiElement element,
                                               @Nullable YAMLKeyValue flowKv,
                                               @NotNull VariableCollector collector) {
        collectBuiltInVars(collector);
        collectArguments(element, collector);
        if (flowKv != null) {
            collectFlowInputParams(flowKv, collector);
        }
    }

    private static void collectVariablesFromScope(YAMLSequenceItem startItem, VariableCollector collector) {
        var metaProvider = ConcordMetaTypeProvider.getInstance(collector.project);
        var currentItem = startItem;

        while (currentItem != null) {
            if (currentItem.getParent() instanceof YAMLSequence sequence) {
                if (metaProvider.getResolvedMetaType(currentItem) instanceof StepElementMetaType) {
                    collectLocalStepVars(currentItem, collector);
                    collectPreviousSiblings(sequence, currentItem, collector);
                }
                currentItem = PsiTreeUtil.getParentOfType(sequence, YAMLSequenceItem.class);
            } else {
                break;
            }
        }
    }

    private static void collectPreviousSiblings(YAMLSequence sequence, YAMLSequenceItem currentItem, VariableCollector collector) {
        for (var item : sequence.getItems()) {
            if (item == currentItem) {
                break;
            }
            if (item.getValue() instanceof YAMLMapping mapping) {
                processStepMapping(mapping, collector);
            }
        }
    }

    private static void processStepMapping(YAMLMapping mapping, VariableCollector collector) {
        if (mapping.getKeyValueByKey("switch") != null) {
            mapping.getKeyValues().stream()
                    .filter(kv -> !"switch".equals(kv.getKeyText()))
                    .forEach(kv -> collectFromBranch(kv.getValue(), collector));
            return;
        }

        var resolver = createOutResolver(mapping.getKeyValueByKey("call"),
                mapping.getKeyValueByKey("task"));

        for (var kv : mapping.getKeyValues()) {
            var value = kv.getValue();
            if (value == null) {
                continue;
            }

            switch (kv.getKeyText()) {
                case "set" -> collectSetVars(value, collector);
                case "out" -> extractOutVars(value, collector, resolver);
                case "then", "else", "try", "error", "block" -> collectFromBranch(value, collector);
            }
        }
    }

    private static void collectBuiltInVars(VariableCollector collector) {
        BuiltInVarsSchema.getInstance().getBuiltInVars().properties().forEach((name, prop) ->
                collector.add(new Variable(name, VariableSource.BUILT_IN, null, prop)));
    }

    private static void collectArguments(PsiElement context, VariableCollector collector) {
        getCachedArgumentVariables(context).forEach(collector::add);
    }

    private static @NotNull List<Variable> getCachedArgumentVariables(@NotNull PsiElement context) {
        var psiFile = context.getContainingFile();
        if (psiFile == null) {
            return List.of();
        }

        return CachedValuesManager.getCachedValue(psiFile, ARG_VARS_KEY, () -> {
            var project = psiFile.getProject();
            var args = ArgumentsCollector.getInstance(project).getArguments(psiFile);
            if (args.isEmpty()) {
                var tracker = ConcordModificationTracker.getInstance(project);
                return CachedValueProvider.Result.create(List.of(), tracker.structure(), tracker.arguments());
            }
            var vars = new ArrayList<Variable>(args.size());
            args.forEach((name, kv) ->
                    vars.add(new Variable(name, VariableSource.ARGUMENT, kv, SchemaInference.inferSchema(name, kv.getValue()))));
            var tracker = ConcordModificationTracker.getInstance(project);
            return CachedValueProvider.Result.create(List.copyOf(vars), tracker.structure(), tracker.arguments());
        });
    }

    private static void collectLocalStepVars(YAMLSequenceItem stepItem, VariableCollector collector) {
        if (stepItem.getValue() instanceof YAMLMapping m) {
            var loopKv = m.getKeyValueByKey("loop");
            if (loopKv != null) {
                BuiltInVarsSchema.getInstance().getLoopVars().properties().forEach((name, prop) ->
                        collector.add(new Variable(name, VariableSource.LOOP, loopKv, prop)));
            }
        }
    }

    private static void collectSetVars(YAMLValue value, VariableCollector collector) {
        if (value instanceof YAMLMapping m) {
            Map<String, DottedKeyNode> roots = new LinkedHashMap<>();

            for (var entry : m.getKeyValues()) {
                var name = entry.getKeyText().trim();
                if (name.isEmpty()) {
                    continue;
                }

                var segments = name.split("\\.", -1);
                if (segments.length > 1 && allSegmentsNonEmpty(segments)) {
                    roots.computeIfAbsent(segments[0], k -> new DottedKeyNode())
                            .insert(segments, 1, SchemaInference.inferSchema(name, entry.getValue()).schemaType());
                } else {
                    collector.add(new Variable(name, VariableSource.SET_STEP, entry, SchemaInference.inferSchema(name, entry.getValue())));
                }
            }

            for (var entry : roots.entrySet()) {
                collector.add(new Variable(entry.getKey(), VariableSource.SET_STEP, m,
                        new SchemaProperty(entry.getKey(), entry.getValue().toSchemaType(), null, false)));
            }
        }
    }

    private static boolean allSegmentsNonEmpty(String[] segments) {
        for (var s : segments) {
            if (s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static class DottedKeyNode {

        final Map<String, DottedKeyNode> children = new LinkedHashMap<>();
        SchemaType leafType;

        void insert(String[] segments, int index, SchemaType type) {
            if (index == segments.length) {
                leafType = type;
                return;
            }
            children.computeIfAbsent(segments[index], k -> new DottedKeyNode())
                    .insert(segments, index + 1, type);
        }

        SchemaType toSchemaType() {
            if (children.isEmpty()) {
                return leafType != null ? leafType : SchemaType.ANY;
            }
            var props = new LinkedHashMap<String, SchemaProperty>();
            for (var entry : children.entrySet()) {
                props.put(entry.getKey(), new SchemaProperty(
                        entry.getKey(), entry.getValue().toSchemaType(), null, false));
            }
            return new SchemaType.Object(
                    new ObjectSchema(Collections.unmodifiableMap(props), Set.of(), true));
        }
    }

    private static void extractOutVars(YAMLValue value, VariableCollector collector, Function<String, SchemaProperty> resolver) {
        if (value instanceof YAMLScalar s) {
            addOutVar(s.getTextValue(), s, collector, resolver);
        } else if (value instanceof YAMLSequence seq) {
            for (var item : seq.getItems()) {
                if (item.getValue() instanceof YAMLScalar s) {
                    addOutVar(s.getTextValue(), s, collector, resolver);
                }
            }
        } else if (value instanceof YAMLMapping m) {
            for (var kv : m.getKeyValues()) {
                addOutVar(kv.getKeyText(), kv, collector, SchemaProperty::any);
            }
        }
    }

    private static void addOutVar(String name, PsiElement el, VariableCollector collector, Function<String, SchemaProperty> resolver) {
        var trimmed = name.trim();
        if (!trimmed.isEmpty()) {
            collector.add(new Variable(trimmed, VariableSource.STEP_OUT, el, resolver.apply(trimmed)));
        }
    }

    private static void collectFromBranch(YAMLValue branchValue, VariableCollector collector) {
        if (branchValue instanceof YAMLSequence sequence) {
            sequence.getItems().stream()
                    .map(YAMLSequenceItem::getValue)
                    .filter(v -> v instanceof YAMLMapping)
                    .forEach(v -> processStepMapping((YAMLMapping) v, collector));
        }
    }

    private static void collectFlowInputParams(@NotNull YAMLKeyValue flowKv, VariableCollector collector) {
        var doc = FlowCallParamsProvider.findFlowDocumentationBefore(flowKv);
        if (doc != null) {
            doc.getInputParameters().forEach(p ->
                    collector.add(new Variable(p.getName(), VariableSource.FLOW_PARAMETER, p, SchemaInference.fromFlowDocParameter(p))));
        }
    }

    private static @Nullable YAMLSequenceItem findEnclosingStep(@NotNull PsiElement element) {
        var metaProvider = ConcordMetaTypeProvider.getInstance(element.getProject());
        var item = PsiTreeUtil.getParentOfType(element, YAMLSequenceItem.class);
        while (item != null) {
            if (metaProvider.getResolvedMetaType(item) instanceof StepElementMetaType) {
                return item;
            }
            item = PsiTreeUtil.getParentOfType(item, YAMLSequenceItem.class);
        }
        return null;
    }

    private static @NotNull Function<String, SchemaProperty> createOutResolver(@Nullable YAMLKeyValue callKv, @Nullable YAMLKeyValue taskKv) {
        if (callKv != null) {
            var doc = FlowCallParamsProvider.findFlowDocumentation(callKv);
            if (doc != null && !doc.getOutputParameters().isEmpty()) {
                Map<String, SchemaProperty> types = new HashMap<>();
                doc.getOutputParameters().forEach(p -> types.put(p.getName(), SchemaInference.fromFlowDocParameter(p)));
                return name -> types.getOrDefault(name, SchemaProperty.any(name));
            }
        } else if (taskKv != null) {
            var schema = resolveTaskOutType(taskKv);
            return name -> taskResultSchema(name, schema);
        }
        return SchemaProperty::any;
    }

    private static @Nullable ObjectSchema resolveTaskOutType(@NotNull YAMLKeyValue taskKv) {
        var taskName = taskKv.getValueText();
        if (taskName.isBlank()) {
            return null;
        }
        var schema = TaskSchemaRegistry.getInstance(taskKv.getProject()).getSchema(taskName);
        return schema != null ? schema.outSection() : null;
    }

    private static @NotNull SchemaProperty taskResultSchema(String name, @Nullable ObjectSchema properties) {
        if (properties == null) {
            return SchemaProperty.any(name);
        }
        return new SchemaProperty(name, new SchemaType.Object(properties), "task result", false);
    }

    private static @Nullable Variable resolveTaskResult(@NotNull YAMLSequenceItem stepItem, @NotNull PsiElement cursor) {
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

    private static class VariableCollector {

        private final Project project;
        private final Map<String, Variable> vars = new LinkedHashMap<>();

        VariableCollector(Project project) {
            this.project = project;
        }

        void add(Variable v) {
            vars.put(v.name(), v);
        }

        List<Variable> toList() {
            return List.copyOf(vars.values());
        }
    }
}
