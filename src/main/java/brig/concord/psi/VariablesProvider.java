package brig.concord.psi;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.StepElementMetaType;
import brig.concord.yaml.psi.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class VariablesProvider {

    private static final String TASK_RESULT_VAR = "result";

    public record Variable(@NotNull String name, @NotNull VariableSource source, @Nullable PsiElement declaration) {}

    public enum VariableSource {
        BUILT_IN, ARGUMENT, FLOW_PARAMETER, SET_STEP, STEP_OUT, LOOP, TASK_RESULT
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
        for (var v : ConcordBuiltInVars.VARS) {
            result.put(v.name(), new Variable(v.name(), VariableSource.BUILT_IN, null));
        }
    }

    private static void collectLoopVars(Map<String, Variable> result, PsiElement declaration) {
        for (var v : ConcordLoopVars.VARS) {
            result.put(v.name(), new Variable(v.name(), VariableSource.LOOP, declaration));
        }
    }

    private static void collectArguments(PsiElement context, Map<String, Variable> result) {
        var args = ArgumentsCollector.getInstance(context.getProject()).getArguments(context);
        for (var entry : args.entrySet()) {
            var name = entry.getKey();
            var value = entry.getValue();
            var declaration = value != null ? value.getParent() : null;
            result.put(name, new Variable(name, VariableSource.ARGUMENT, declaration));
        }
    }

    private static void collectFlowDocParams(YAMLKeyValue flowKv, Map<String, Variable> result) {
        var doc = FlowCallParamsProvider.findFlowDocumentationBefore(flowKv);
        if (doc == null) {
            return;
        }

        for (var param : doc.getInputParameters()) {
            var name = param.getName();
            result.put(name, new Variable(name, VariableSource.FLOW_PARAMETER, param));
        }
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
                        result.put(TASK_RESULT_VAR, new Variable(TASK_RESULT_VAR, VariableSource.TASK_RESULT, outKv));
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

        for (var kv : stepMapping.getKeyValues()) {
            switch (kv.getKeyText()) {
                case "set" -> collectSetVars(kv.getValue(), result);
                case "out" -> extractOutVars(kv.getValue(), result);
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
                result.put(name, new Variable(name, VariableSource.SET_STEP, entry));
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

    private static void extractOutVars(YAMLValue value, Map<String, Variable> result) {
        if (value instanceof YAMLScalar s) {
            var name = s.getTextValue().trim();
            if (!name.isEmpty()) {
                result.put(name, new Variable(name, VariableSource.STEP_OUT, s));
            }
        } else if (value instanceof YAMLSequence seq) {
            for (var item : seq.getItems()) {
                var itemValue = item.getValue();
                if (itemValue instanceof YAMLScalar s) {
                    var name = s.getTextValue().trim();
                    if (!name.isEmpty()) {
                        result.put(name, new Variable(name, VariableSource.STEP_OUT, s));
                    }
                }
            }
        } else if (value instanceof YAMLMapping m) {
            for (var kv : m.getKeyValues()) {
                var name = kv.getKeyText().trim();
                if (!name.isEmpty()) {
                    result.put(name, new Variable(name, VariableSource.STEP_OUT, kv));
                }
            }
        }
    }
}
