package brig.concord.psi;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.schema.TaskInParamsMetaType;
import brig.concord.schema.TaskOutParamsMetaType;
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

    public record Variable(@NotNull String name, @NotNull VariableSource source, @Nullable PsiElement declaration) {}

    public enum VariableSource {
        BUILT_IN, ARGUMENT, FLOW_PARAMETER, SET_STEP, STEP_OUT, TASK_RESULT
    }

    private VariablesProvider() {}

    public static @NotNull List<Variable> getVariables(@NotNull PsiElement element) {
        Map<String, Variable> result = new LinkedHashMap<>();

        var flowKv = ProcessDefinition.findEnclosingFlowDefinition(element);

        collectBuiltInVars(result);

        if (flowKv != null) {
            collectArguments(element, result);
            collectFlowDocParams(flowKv, result);
            collectFromSteps(element, result);
        }

//        collectTaskResult(element, result);

        return new ArrayList<>(result.values());
    }

    private static void collectBuiltInVars(Map<String, Variable> result) {
        for (var v : ConcordBuiltInVars.VARS) {
            result.put(v.name(), new Variable(v.name(), VariableSource.BUILT_IN, null));
        }
    }

    private static void collectArguments(PsiElement context, Map<String, Variable> result) {
        var args = ArgumentsCollector.getInstance(context.getProject()).getArguments(context);
        for (var entry : args.entrySet()) {
            var name = entry.getKey();
            var value = entry.getValue();
            PsiElement declaration = value != null ? value.getParent() : null;
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
        var current = element;
        while (current != null) {
            var sequenceItem = PsiTreeUtil.getParentOfType(current, YAMLSequenceItem.class);
            if (sequenceItem == null) {
                break;
            }

            var parent = sequenceItem.getParent();
            if (!(parent instanceof YAMLSequence sequence)) {
                break;
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

            var seqParent = sequence.getParent();
            if (seqParent instanceof YAMLKeyValue kv && ProcessDefinition.isFlowDefinition(kv)) {
                break;
            }

            current = sequence;
        }
    }

    private static void collectFromStep(YAMLMapping stepMapping, Map<String, Variable> result) {
        for (var kv : stepMapping.getKeyValues()) {
            var key = kv.getKeyText();
            if ("set".equals(key)) {
                var value = kv.getValue();
                if (value instanceof YAMLMapping m) {
                    for (var entry : m.getKeyValues()) {
                        var name = entry.getKeyText().trim();
                        if (!name.isEmpty()) {
                            result.put(name, new Variable(name, VariableSource.SET_STEP, entry));
                        }
                    }
                }
            } else if ("out".equals(key)) {
                extractOutVars(kv.getValue(), result);
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
