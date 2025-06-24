package brig.concord.psi.ref;

import brig.concord.psi.ProcessDefinition;
import brig.concord.psi.ProcessDefinitionProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLScalar;

public class FlowDefinitionReference extends PsiReferenceBase.Poly<YAMLScalar> implements PsiPolyVariantReference {

    public FlowDefinitionReference(YAMLScalar element) {
        super(element, TextRange.allOf(element.getText()), false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        PsiElement flowDef = CachedValuesManager.getCachedValue(getElement(), new FlowDefinitionCachedValueProvider(getElement()));
        if (flowDef == null) {
            return ResolveResult.EMPTY_ARRAY;
        }
        return PsiElementResolveResult.createResults(flowDef);
    }

    private static final class FlowDefinitionCachedValueProvider implements CachedValueProvider<PsiElement> {

        private final YAMLScalar callElement;

        private FlowDefinitionCachedValueProvider(YAMLScalar callElement) {
            this.callElement = callElement;
        }

        @Override
        public Result<PsiElement> compute() {
            return CachedValueProvider.Result.create(resolveInner(), PsiModificationTracker.MODIFICATION_COUNT, callElement);
        }

        private PsiElement resolveInner() {
            ProcessDefinition process = ProcessDefinitionProvider.getInstance().get(callElement);
            if (process == null) {
                return null;
            }

            String flowName = callElement.getTextValue();
            return process.flow(flowName);
        }
    }
}

