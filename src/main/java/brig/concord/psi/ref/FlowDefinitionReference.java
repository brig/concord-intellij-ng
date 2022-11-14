package brig.concord.psi.ref;

import brig.concord.psi.ProcessDefinition;
import brig.concord.psi.ProcessDefinitionProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

public class FlowDefinitionReference extends PsiReferenceBase.Poly<YAMLScalar> implements PsiPolyVariantReference {

    public FlowDefinitionReference(YAMLScalar element) {
        super(element, TextRange.allOf(element.getText()), false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        ProcessDefinition process = ProcessDefinitionProvider.getInstance().get(getElement());
        if (process == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        String flowName = getElement().getTextValue();
        PsiElement flowDef = process.flow(flowName);
        if (flowDef == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        return PsiElementResolveResult.createResults(flowDef);
    }
}

