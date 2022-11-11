package brig.concord.psi.ref;

import brig.concord.psi.ProcessDefinition;
import brig.concord.psi.ProcessDefinitionProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

public class FlowDefinitionReference extends PsiReferenceBase.Poly<YAMLKeyValue> implements PsiPolyVariantReference {

    public FlowDefinitionReference(@NotNull YAMLKeyValue element,
                                   @NotNull TextRange textRange) {
        super(element, textRange, false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        ProcessDefinition process = ProcessDefinitionProvider.getInstance().get(getElement());
        if (process == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        String flowName = getElement().getValueText();
        PsiElement flowDef = process.flow(flowName);
        if (flowDef == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        return PsiElementResolveResult.createResults(flowDef);
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        return super.isReferenceTo(element);
    }


}
