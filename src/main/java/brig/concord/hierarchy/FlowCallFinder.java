package brig.concord.hierarchy;

import brig.concord.meta.model.*;
import brig.concord.meta.model.call.CallStepMetaType;
import brig.concord.psi.ProcessDefinition;
import brig.concord.psi.ProcessDefinitionProvider;
import brig.concord.psi.YamlPsiUtils;
import brig.concord.yaml.psi.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class FlowCallFinder {

    private static final String CALL_KEY = CallStepMetaType.getInstance().getIdentity();
    private static final String SWITCH_KEY = SwitchStepMetaType.getInstance().getIdentity();
    private static final Set<String> NESTED_STEP_KEYS = Set.of(
            GroupOfStepsMetaType.ERROR,
            TryStepMetaType.getInstance().getIdentity(),
            BlockStepMetaType.getInstance().getIdentity(),
            ParallelStepMetaType.getInstance().getIdentity(),
            IfStepMetaType.THEN,
            IfStepMetaType.ELSE
    );

    private FlowCallFinder() {
    }

    public record CallSite(
            @NotNull YAMLKeyValue callKeyValue,
            @NotNull String flowName,
            boolean isDynamic
    ) {
        public @NotNull PsiFile getContainingFile() {
            return callKeyValue.getContainingFile();
        }
    }

    /**
     * Find all places where the given flow is called.
     * Uses ReferencesSearch to find all references to the flow definition.
     *
     * @param flowDefinition the flow definition (YAMLKeyValue under /flows)
     * @param scope          the search scope
     * @return list of call sites where this flow is called
     */
    public static @NotNull List<CallSite> findCallers(
            @NotNull YAMLKeyValue flowDefinition,
            @NotNull GlobalSearchScope scope) {

        var project = flowDefinition.getProject();
        if (ActionUtil.isDumbMode(project)) {
            return List.of();
        }

        var flowName = flowDefinition.getKeyText();
        List<CallSite> result = new ArrayList<>();

        ReferencesSearch.search(flowDefinition, scope).forEach(reference -> {
            var element = reference.getElement();
            var callKv = findCallKeyValue(element);
            if (callKv != null) {
                result.add(new CallSite(callKv, flowName, false));
            }
            return true;
        });

        return result;
    }

    /**
     * Find all flows that are called from within the given flow's steps.
     *
     * @param flowKeyValue the flow definition (YAMLKeyValue under /flows)
     * @return list of call sites within this flow
     */
    public static @NotNull List<CallSite> findCallees(@NotNull YAMLKeyValue flowKeyValue) {
        var value = flowKeyValue.getValue();
        if (!(value instanceof YAMLSequence stepsSequence)) {
            return List.of();
        }

        var result = new ArrayList<CallSite>();
        collectCallsFromSteps(stepsSequence, result);
        return result;
    }

    /**
     * Find the containing flow definition for a given element.
     *
     * @param element any PSI element within a Concord file
     * @return the YAMLKeyValue representing the flow definition, or null if not inside a flow
     */
    public static @Nullable YAMLKeyValue findContainingFlow(@NotNull PsiElement element) {
        var current = element;
        while (current != null) {
            if (current instanceof YAMLKeyValue kv && ProcessDefinition.isFlowDefinition(kv)) {
                return kv;
            }
            current = current.getParent();
        }
        return null;
    }

    /**
     * Resolve a call site to its flow definition using existing references.
     *
     * @param callSite the call site
     * @return the flow definition, or null if not resolvable (e.g., dynamic expression)
     */
    public static @Nullable YAMLKeyValue resolveCallToFlow(@NotNull CallSite callSite) {
        if (callSite.isDynamic()) {
            return null;
        }

        var callKv = callSite.callKeyValue();
        var value = callKv.getValue();

        if (value instanceof YAMLScalar scalar) {
            var references = scalar.getReferences();
            for (var ref : references) {
                var resolved = ref.resolve();
                if (resolved instanceof YAMLKeyValue flowKv && ProcessDefinition.isFlowDefinition(flowKv)) {
                    return flowKv;
                }
            }
        }

        // Fallback: use ProcessDefinition
        var process = ProcessDefinitionProvider.getInstance().get(callKv);
        if (process != null) {
            var flow = process.flow(callSite.flowName());
            if (flow instanceof YAMLKeyValue flowKv) {
                return flowKv;
            }
        }

        return null;
    }

    /**
     * Get the flow definition from the element (either the element itself or its container).
     */
    public static @Nullable YAMLKeyValue getFlowDefinition(@NotNull PsiElement element) {
        if (element instanceof YAMLKeyValue kv && ProcessDefinition.isFlowDefinition(kv)) {
            return kv;
        }
        return findContainingFlow(element);
    }

    /**
     * Get the name of the flow from the element.
     */
    public static @Nullable String getFlowName(@NotNull PsiElement element) {
        var flowKv = getFlowDefinition(element);
        return flowKv != null ? flowKv.getKeyText() : null;
    }

    /**
     * Find the call: key-value that contains the given element.
     */
    private static @Nullable YAMLKeyValue findCallKeyValue(@NotNull PsiElement element) {
        // Element is typically the scalar value of call: flowName
        // Walk up to find the YAMLKeyValue with key "call"
        var parent = element.getParent();
        if (parent instanceof YAMLKeyValue kv && CALL_KEY.equals(kv.getKeyText())) {
            return kv;
        }
        // Try one more level up (for quoted strings)
        if (parent != null) {
            var grandParent = parent.getParent();
            if (grandParent instanceof YAMLKeyValue kv && CALL_KEY.equals(kv.getKeyText())) {
                return kv;
            }
        }
        return null;
    }

    private static void collectCallsFromSteps(@NotNull YAMLSequence stepsSequence, @NotNull List<CallSite> result) {
        for (var item : stepsSequence.getItems()) {
            var value = item.getValue();
            if (value instanceof YAMLMapping mapping) {
                collectCallsFromStepMapping(mapping, result);
            }
        }
    }

    private static void collectCallsFromStepMapping(@NotNull YAMLMapping mapping, @NotNull List<CallSite> result) {
        // Check if this is a switch step - if so, all non-switch keys are case labels with steps
        boolean isSwitchStep = mapping.getKeyValueByKey(SWITCH_KEY) != null;

        for (var kv : mapping.getKeyValues()) {
            var keyText = kv.getKeyText();

            if (CALL_KEY.equals(keyText)) {
                var value = kv.getValue();
                if (value instanceof YAMLScalar scalar) {
                    var callTarget = scalar.getTextValue();
                    var isDynamic = YamlPsiUtils.isDynamicExpression(callTarget);
                    result.add(new CallSite(kv, callTarget, isDynamic));
                }
            } else if (NESTED_STEP_KEYS.contains(keyText)) {
                var nestedValue = kv.getValue();
                if (nestedValue instanceof YAMLSequence nestedSeq) {
                    collectCallsFromSteps(nestedSeq, result);
                }
            } else if (isSwitchStep && !SWITCH_KEY.equals(keyText)) {
                // Case labels in switch step - their values are step sequences
                var nestedValue = kv.getValue();
                if (nestedValue instanceof YAMLSequence nestedSeq) {
                    collectCallsFromSteps(nestedSeq, result);
                }
            }
        }
    }
}
