package brig.concord.psi.ref;

import brig.concord.completion.provider.FlowCallParamsProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLKeyValue;

public class CallInParamDefinitionReference extends PsiReferenceBase.Poly<YAMLKeyValue> implements PsiPolyVariantReference {

    public CallInParamDefinitionReference(YAMLKeyValue element) {
        super(element, TextRange.allOf(element.getKeyText()), false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        PsiElement inParamDef = CachedValuesManager.getCachedValue(getElement(), new InParamsDefinitionCachedValueProvider(getElement()));

        if (inParamDef == null) {
            return ResolveResult.EMPTY_ARRAY;
        }

        return PsiElementResolveResult.createResults(inParamDef);
    }

    private static final class InParamsDefinitionCachedValueProvider implements CachedValueProvider<PsiElement> {

        private final YAMLKeyValue inParamElement;

        private InParamsDefinitionCachedValueProvider(YAMLKeyValue inParamElement) {
            this.inParamElement = inParamElement;
        }

        @Override
        public Result<PsiElement> compute() {
            PsiElement callDefinition = FlowCallParamsProvider.findCallKv(inParamElement);
            if (callDefinition != null) {
                return CachedValueProvider.Result.create(resolveInner(), PsiModificationTracker.MODIFICATION_COUNT, callDefinition);
            } else {
                return CachedValueProvider.Result.create(null, PsiModificationTracker.MODIFICATION_COUNT, inParamElement);
            }
        }

        private PsiElement resolveInner() {
            return FlowCallParamsProvider.getInstance().inParamDefinition(inParamElement);
        }
    }
}

