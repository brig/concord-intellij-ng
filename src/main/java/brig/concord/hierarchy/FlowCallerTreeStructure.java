package brig.concord.hierarchy;

import brig.concord.psi.ConcordScopeService;
import brig.concord.psi.ProcessDefinition;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Tree structure for "Callers" view in flow hierarchy.
 * Shows all flows that call the selected flow.
 */
public class FlowCallerTreeStructure extends HierarchyTreeStructure {

    private final Set<String> visitedFlows = new HashSet<>();

    public FlowCallerTreeStructure(@NotNull Project project, @NotNull PsiElement element) {
        super(project, createBaseDescriptor(project, element));
        var flowName = extractFlowName(element);
        if (flowName != null) {
            visitedFlows.add(flowName);
        }
    }

    @Override
    protected Object @NotNull [] buildChildren(@NotNull HierarchyNodeDescriptor descriptor) {
        var element = descriptor.getPsiElement();
        if (element == null || !element.isValid()) {
            return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
        }

        // Get the flow name from the descriptor
        String flowName;
        if (descriptor instanceof FlowHierarchyNodeDescriptor flowDesc) {
            if (flowDesc.isDynamic()) {
                // Can't find callers for dynamic flows
                return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
            }
            flowName = flowDesc.getFlowName();
        } else {
            flowName = extractFlowName(element);
        }

        if (flowName == null) {
            return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
        }

        var scope = ConcordScopeService.getInstance(myProject).createSearchScope(element);
        var callers = FlowCallFinder.findCallers(myProject, flowName, scope);

        if (callers.isEmpty()) {
            return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
        }

        // Group call sites by containing flow
        Map<YAMLKeyValue, List<FlowCallFinder.CallSite>> callersByFlow = new LinkedHashMap<>();
        for (var callSite : callers) {
            var containingFlow = FlowCallFinder.findContainingFlow(callSite.callKeyValue());
            if (containingFlow != null) {
                callersByFlow.computeIfAbsent(containingFlow, k -> new ArrayList<>()).add(callSite);
            }
        }

        List<FlowHierarchyNodeDescriptor> children = new ArrayList<>();
        for (var entry : callersByFlow.entrySet()) {
            var callerFlow = entry.getKey();
            var callerFlowName = callerFlow.getKeyText();

            // Prevent infinite loops for recursive calls
            if (visitedFlows.contains(callerFlowName)) {
                continue;
            }
            visitedFlows.add(callerFlowName);

            // Use the first call site as the representative element
            var callSite = entry.getValue().getFirst();
            var child = new FlowHierarchyNodeDescriptor(
                    myProject,
                    descriptor,
                    callerFlow,
                    false,
                    false,
                    callerFlowName
            );
            children.add(child);
        }

        return children.toArray(FlowHierarchyNodeDescriptor[]::new);
    }

    private static @NotNull FlowHierarchyNodeDescriptor createBaseDescriptor(
            @NotNull Project project,
            @NotNull PsiElement element) {
        var flowKv = getFlowKeyValue(element);
        var flowName = flowKv != null ? flowKv.getKeyText() : "<unknown>";
        return new FlowHierarchyNodeDescriptor(
                project,
                null,
                flowKv != null ? flowKv : element,
                true,
                false,
                flowName
        );
    }

    private static String extractFlowName(@NotNull PsiElement element) {
        var flowKv = getFlowKeyValue(element);
        return flowKv != null ? flowKv.getKeyText() : null;
    }

    private static YAMLKeyValue getFlowKeyValue(@NotNull PsiElement element) {
        if (element instanceof YAMLKeyValue kv && ProcessDefinition.isFlowDefinition(kv)) {
            return kv;
        }
        return FlowCallFinder.findContainingFlow(element);
    }
}
