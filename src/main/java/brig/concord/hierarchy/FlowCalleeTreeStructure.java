// SPDX-License-Identifier: Apache-2.0
package brig.concord.hierarchy;

import brig.concord.psi.ProcessDefinition;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static brig.concord.psi.ProcessDefinition.findEnclosingFlowDefinition;

/**
 * Tree structure for "Callees" view in flow hierarchy.
 * Shows all flows that are called from within the selected flow.
 */
public class FlowCalleeTreeStructure extends HierarchyTreeStructure {

    private final Set<String> visitedFlows = new HashSet<>();

    public FlowCalleeTreeStructure(@NotNull Project project, @NotNull PsiElement element) {
        super(project, createBaseDescriptor(project, element));
        var flowName = FlowCallFinder.getFlowName(element);
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

        // Dynamic flows can't have children resolved
        if (descriptor instanceof FlowHierarchyNodeDescriptor flowDesc && flowDesc.isDynamic()) {
            return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
        }

        // Get the flow definition
        var flowKv = findEnclosingFlowDefinition(element);
        if (flowKv == null) {
            return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
        }

        var callees = FlowCallFinder.findCallees(flowKv);
        if (callees.isEmpty()) {
            return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
        }

        List<FlowHierarchyNodeDescriptor> children = new ArrayList<>();
        Set<String> addedFlows = new HashSet<>();

        for (var callSite : callees) {
            var callTarget = callSite.flowName();

            // Deduplicate - only show each callee once per parent
            if (addedFlows.contains(callTarget)) {
                continue;
            }
            addedFlows.add(callTarget);

            if (callSite.isDynamic()) {
                // For dynamic calls, create a node with the call site as element
                var child = new FlowHierarchyNodeDescriptor(
                        myProject,
                        descriptor,
                        callSite.callKeyValue(),
                        false,
                        true,
                        callTarget
                );
                children.add(child);
            } else {
                // Prevent infinite loops for recursive calls
                if (visitedFlows.contains(callTarget)) {
                    continue;
                }
                visitedFlows.add(callTarget);

                // Resolve to flow definition
                var targetFlow = FlowCallFinder.resolveCallToFlow(callSite);
                if (targetFlow != null) {
                    var child = new FlowHierarchyNodeDescriptor(
                            myProject,
                            descriptor,
                            targetFlow,
                            false,
                            false,
                            callTarget
                    );
                    children.add(child);
                } else {
                    // Flow not found - show the call site with a marker
                    var child = new FlowHierarchyNodeDescriptor(
                            myProject,
                            descriptor,
                            callSite.callKeyValue(),
                            false,
                            false,
                            callTarget
                    );
                    children.add(child);
                }
            }
        }

        return children.toArray(FlowHierarchyNodeDescriptor[]::new);
    }

    private static @NotNull FlowHierarchyNodeDescriptor createBaseDescriptor(
            @NotNull Project project,
            @NotNull PsiElement element) {
        var flowKv = findEnclosingFlowDefinition(element);
        var flowName = flowKv != null ? flowKv.getKeyText() : FlowHierarchyNodeDescriptor.UNKNOWN_FLOW;
        return new FlowHierarchyNodeDescriptor(
                project,
                null,
                flowKv != null ? flowKv : element,
                true,
                false,
                flowName
        );
    }
}
