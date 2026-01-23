package brig.concord.hierarchy;

import brig.concord.psi.ProcessDefinition;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.ide.hierarchy.CallHierarchyBrowserBase;
import com.intellij.ide.hierarchy.HierarchyNodeDescriptor;
import com.intellij.ide.hierarchy.HierarchyTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Comparator;
import java.util.Map;

/**
 * Browser component for flow call hierarchy.
 * Provides "Callers" and "Callees" views for Concord flows.
 */
public class FlowCallHierarchyBrowser extends CallHierarchyBrowserBase {

    public FlowCallHierarchyBrowser(@NotNull Project project, @NotNull PsiElement target) {
        super(project, target);
    }

    @Override
    protected @Nullable HierarchyTreeStructure createHierarchyTreeStructure(
            @NotNull String type,
            @NotNull PsiElement psiElement) {
        if (getCallerType().equals(type)) {
            return new FlowCallerTreeStructure(myProject, psiElement);
        } else if (getCalleeType().equals(type)) {
            return new FlowCalleeTreeStructure(myProject, psiElement);
        }
        return null;
    }

    @Override
    protected void createTrees(@NotNull Map<? super String, ? super JTree> trees) {
        createTreeAndSetupStandardActions(trees, getCallerType());
        createTreeAndSetupStandardActions(trees, getCalleeType());
    }

    private void createTreeAndSetupStandardActions(@NotNull Map<? super String, ? super JTree> trees, String type) {
        var tree = createTree(false);
        trees.put(type, tree);
    }

    @Override
    protected @Nullable PsiElement getElementFromDescriptor(@NotNull HierarchyNodeDescriptor descriptor) {
        return descriptor.getPsiElement();
    }

    @Override
    protected boolean isApplicableElement(@NotNull PsiElement element) {
        if (element instanceof YAMLKeyValue kv) {
            return ProcessDefinition.isFlowDefinition(kv);
        }
        // Also allow if we're inside a flow
        return FlowCallFinder.findContainingFlow(element) != null;
    }

    @Override
    protected @Nullable JPanel createLegendPanel() {
        return null;
    }

    @Override
    protected @Nullable Comparator<NodeDescriptor<?>> getComparator() {
        // Sort by flow name alphabetically
        return (o1, o2) -> {
            if (o1 instanceof FlowHierarchyNodeDescriptor d1 && o2 instanceof FlowHierarchyNodeDescriptor d2) {
                return d1.getFlowName().compareToIgnoreCase(d2.getFlowName());
            }
            return 0;
        };
    }
}
