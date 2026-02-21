// SPDX-License-Identifier: Apache-2.0
package brig.concord.hierarchy;

import brig.concord.ConcordFileType;
import brig.concord.meta.model.call.CallStepMetaType;
import brig.concord.psi.ProcessDefinition;
import brig.concord.psi.ProcessDefinitionProvider;
import brig.concord.psi.YamlPsiUtils;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.ide.hierarchy.CallHierarchyBrowserBase;
import com.intellij.ide.hierarchy.HierarchyBrowser;
import com.intellij.ide.hierarchy.HierarchyProvider;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static brig.concord.psi.ProcessDefinition.findEnclosingFlowDefinition;

/**
 * Entry point for Call Hierarchy feature for Concord flows.
 * Activated when user invokes "Call Hierarchy" (Ctrl+Alt+H) on a flow.
 */
public class FlowCallHierarchyProvider implements HierarchyProvider {

    @Override
    public @Nullable PsiElement getTarget(@NotNull DataContext dataContext) {
        var project = CommonDataKeys.PROJECT.getData(dataContext);
        var editor = CommonDataKeys.EDITOR.getData(dataContext);
        var psiFile = CommonDataKeys.PSI_FILE.getData(dataContext);

        if (project == null || psiFile == null) {
            return null;
        }

        // Check if it's a Concord file
        if (!isConcordFile(psiFile)) {
            return null;
        }

        PsiElement element = null;
        if (editor != null) {
            var offset = editor.getCaretModel().getOffset();
            element = psiFile.findElementAt(offset);
        }

        if (element == null) {
            element = CommonDataKeys.PSI_ELEMENT.getData(dataContext);
        }

        if (element == null) {
            return null;
        }

        // Check if we're on a flow definition
        var flowKv = findFlowDefinition(element);
        if (flowKv != null) {
            return flowKv;
        }

        // Check if we're on a call site (call: flowName)
        var callTarget = findCallTarget(element);
        if (callTarget != null) {
            // Try to resolve to flow definition
            var resolved = resolveCallTarget(callTarget);
            if (resolved != null) {
                return resolved;
            }
            // If can't resolve, return the call site element's containing flow
            var containingFlow = findEnclosingFlowDefinition(callTarget);
            return containingFlow != null ? containingFlow : callTarget;
        }

        return null;
    }

    @Override
    public @NotNull HierarchyBrowser createHierarchyBrowser(@NotNull PsiElement target) {
        return new FlowCallHierarchyBrowser(target.getProject(), target);
    }

    @Override
    public void browserActivated(@NotNull HierarchyBrowser hierarchyBrowser) {
        if (hierarchyBrowser instanceof FlowCallHierarchyBrowser browser) {
            browser.changeView(CallHierarchyBrowserBase.getCallerType());
        }
    }

    private static boolean isConcordFile(@NotNull PsiFile file) {
        var virtualFile = file.getVirtualFile();
        if (virtualFile == null) {
            return false;
        }
        return virtualFile.getFileType() instanceof ConcordFileType;
    }

    /**
     * Find the flow definition that contains or is the given element.
     */
    private static @Nullable YAMLKeyValue findFlowDefinition(@NotNull PsiElement element) {
        // Walk up to find YAMLKeyValue
        var current = element;
        while (current != null) {
            if (current instanceof YAMLKeyValue kv) {
                if (ProcessDefinition.isFlowDefinition(kv)) {
                    return kv;
                }
                // Also check if we're on the key element of a flow definition
                var parent = kv.getParent();
                if (parent != null && parent.getParent() instanceof YAMLKeyValue parentKv) {
                    if (ProcessDefinition.isFlowDefinition(parentKv)) {
                        return parentKv;
                    }
                }
            }
            current = current.getParent();
        }

        // Check if element is the key text itself
        var parent = element.getParent();
        if (parent instanceof YAMLKeyValue kv && ProcessDefinition.isFlowDefinition(kv)) {
            return kv;
        }

        return null;
    }

    /**
     * Find the call target if element is inside a call: value.
     */
    private static @Nullable YAMLScalar findCallTarget(@NotNull PsiElement element) {
        // Check if we're inside a YAMLScalar that's the value of a call: key
        var scalar = PsiTreeUtil.getParentOfType(element, YAMLScalar.class, false);
        if (scalar == null) {
            // Element might be the scalar itself
            if (element instanceof YAMLScalar s) {
                scalar = s;
            }
        }

        if (scalar == null) {
            return null;
        }

        var parent = scalar.getParent();
        if (parent instanceof YAMLKeyValue kv) {
            if (CallStepMetaType.getInstance().getIdentity().equals(kv.getKeyText())) {
                return scalar;
            }
        }

        return null;
    }

    /**
     * Resolve a call target to its flow definition.
     */
    private static @Nullable YAMLKeyValue resolveCallTarget(@NotNull YAMLScalar callTarget) {
        if (YamlPsiUtils.isDynamicExpression(callTarget)) {
            // Dynamic expression - can't resolve
            return null;
        }

        var value = callTarget.getTextValue();
        var process = ProcessDefinitionProvider.getInstance().get(callTarget);
        var flowElement = process.flow(value);
        if (flowElement instanceof YAMLKeyValue flowKv) {
            return flowKv;
        }

        return null;
    }
}
