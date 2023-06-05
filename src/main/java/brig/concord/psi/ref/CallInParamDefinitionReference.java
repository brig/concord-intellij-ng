package brig.concord.psi.ref;

import brig.concord.completion.provider.FlowCallParamsProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

public class CallInParamDefinitionReference extends PsiReferenceBase.Poly<YAMLKeyValue> implements PsiPolyVariantReference {

    public CallInParamDefinitionReference(YAMLKeyValue element) {
        super(element, TextRange.allOf(element.getKeyText()), false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        PsiElement inParamDefinition = FlowCallParamsProvider.getInstance().inParamDefinition(getElement());
        if (inParamDefinition == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        return PsiElementResolveResult.createResults(inParamDefinition);
    }
}

