package brig.concord.hierarchy;

import brig.concord.ConcordFileType;
import brig.concord.psi.ConcordScopeService;
import brig.concord.psi.ProcessDefinition;
import brig.concord.yaml.psi.*;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Utility class for finding flow call relationships.
 * Used by Call Hierarchy feature to build caller/callee trees.
 */
public final class FlowCallFinder {

    private static final String CALL_KEY = "call";
    private static final String FLOWS_KEY = "flows";
    private static final Set<String> NESTED_STEP_KEYS = Set.of(
            "error", "try", "block", "parallel", "then", "else"
    );

    private FlowCallFinder() {
    }

    /**
     * Represents a call site where a flow is invoked.
     */
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
     *
     * @param project  the project
     * @param flowName the name of the flow to search for callers
     * @param scope    the search scope
     * @return list of call sites where this flow is called
     */
    public static @NotNull List<CallSite> findCallers(
            @NotNull Project project,
            @NotNull String flowName,
            @NotNull GlobalSearchScope scope) {

        if (ActionUtil.isDumbMode(project)) {
            return List.of();
        }

        List<CallSite> result = new ArrayList<>();
        var psiManager = PsiManager.getInstance(project);

        FileTypeIndex.processFiles(ConcordFileType.INSTANCE, file -> {
            var psiFile = psiManager.findFile(file);
            if (psiFile == null) {
                return true;
            }

            // Find all call: key-values in this file
            var callKeyValues = findAllCallKeyValues(psiFile);
            for (var callKv : callKeyValues) {
                var value = callKv.getValue();
                if (value instanceof YAMLScalar scalar) {
                    var callTarget = scalar.getTextValue();
                    if (isDynamicExpression(callTarget)) {
                        // Dynamic expressions can't be resolved statically
                        // but we include them in callers if the flow name appears in the expression
                        if (callTarget.contains(flowName)) {
                            result.add(new CallSite(callKv, callTarget, true));
                        }
                    } else if (flowName.equals(callTarget)) {
                        result.add(new CallSite(callKv, flowName, false));
                    }
                }
            }
            return true;
        }, scope);

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

        List<CallSite> result = new ArrayList<>();
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
     * Resolve a call site to its flow definition.
     *
     * @param callSite the call site
     * @return the flow definition, or null if not resolvable (e.g., dynamic expression)
     */
    public static @Nullable YAMLKeyValue resolveCallToFlow(@NotNull CallSite callSite) {
        if (callSite.isDynamic()) {
            return null;
        }

        var element = callSite.callKeyValue();
        var project = element.getProject();

        if (ActionUtil.isDumbMode(project)) {
            return null;
        }

        var scope = ConcordScopeService.getInstance(project).createSearchScope(element);
        var files = new ArrayList<com.intellij.openapi.vfs.VirtualFile>();

        com.intellij.util.indexing.FileBasedIndex.getInstance().getFilesWithKey(
                brig.concord.navigation.FlowNamesIndex.KEY,
                Collections.singleton(callSite.flowName()),
                files::add,
                scope
        );

        var psiManager = PsiManager.getInstance(project);
        for (var file : files) {
            var psiFile = psiManager.findFile(file);
            if (psiFile == null) {
                continue;
            }

            var doc = PsiTreeUtil.getChildOfType(psiFile, YAMLDocument.class);
            var flowKv = findFlowInDocument(doc, callSite.flowName());
            if (flowKv != null) {
                return flowKv;
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
        for (var kv : mapping.getKeyValues()) {
            var keyText = kv.getKeyText();

            if (CALL_KEY.equals(keyText)) {
                var value = kv.getValue();
                if (value instanceof YAMLScalar scalar) {
                    var callTarget = scalar.getTextValue();
                    var isDynamic = isDynamicExpression(callTarget);
                    result.add(new CallSite(kv, callTarget, isDynamic));
                }
            } else if (NESTED_STEP_KEYS.contains(keyText)) {
                // Recursively process nested steps
                var nestedValue = kv.getValue();
                if (nestedValue instanceof YAMLSequence nestedSeq) {
                    collectCallsFromSteps(nestedSeq, result);
                }
            }
        }
    }

    private static @NotNull List<YAMLKeyValue> findAllCallKeyValues(@NotNull PsiFile file) {
        List<YAMLKeyValue> result = new ArrayList<>();
        file.accept(new YamlRecursivePsiElementVisitor() {
            @Override
            public void visitKeyValue(@NotNull YAMLKeyValue keyValue) {
                if (CALL_KEY.equals(keyValue.getKeyText())) {
                    result.add(keyValue);
                }
                super.visitKeyValue(keyValue);
            }
        });
        return result;
    }

    private static @Nullable YAMLKeyValue findFlowInDocument(@Nullable PsiElement doc, @NotNull String flowName) {
        if (doc == null) {
            return null;
        }

        // Navigate: Document -> root mapping -> flows key -> flows mapping -> flow key
        if (doc instanceof YAMLDocument yamlDoc) {
            var topValue = yamlDoc.getTopLevelValue();
            if (topValue instanceof YAMLMapping rootMapping) {
                var flowsKv = rootMapping.getKeyValueByKey(FLOWS_KEY);
                if (flowsKv != null) {
                    var flowsValue = flowsKv.getValue();
                    if (flowsValue instanceof YAMLMapping flowsMapping) {
                        return flowsMapping.getKeyValueByKey(flowName);
                    }
                }
            }
        }
        return null;
    }

    private static boolean isDynamicExpression(@NotNull String value) {
        return value.contains("${");
    }
}
