package brig.concord.psi.ref;

import brig.concord.psi.ProcessDefinitionProvider;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLScalar;

public class FlowDefinitionReference extends PsiReferenceBase.Poly<YAMLScalar> implements PsiPolyVariantReference {

    private static final Key<CachedValue<PsiElement>> FLOW_DEF_CACHE = Key.create("concord.flow.def");

    public FlowDefinitionReference(YAMLScalar element) {
        super(element, TextRange.allOf(element.getText()), false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        var el = getElement();

        var flowDef = CachedValuesManager.getCachedValue(el, FLOW_DEF_CACHE, () -> {
            var resolved = resolveInner(el);

            return CachedValueProvider.Result.create(
                    resolved,
                    PsiModificationTracker.MODIFICATION_COUNT,
                    el
            );
        });

        return flowDef == null ? ResolveResult.EMPTY_ARRAY : PsiElementResolveResult.createResults(flowDef);
    }

    private static PsiElement resolveInner(@NotNull YAMLScalar callElement) {
        var process = ProcessDefinitionProvider.getInstance().get(callElement);
        if (process == null) {
            return null;
        }

        var flowName = callElement.getTextValue();
        return process.flow(flowName);
    }
}

