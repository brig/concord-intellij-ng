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

import java.util.List;

public class FlowDefinitionReference extends PsiReferenceBase.Poly<YAMLScalar> implements PsiPolyVariantReference {

    private static final Key<CachedValue<List<PsiElement>>> FLOW_DEFS_CACHE = Key.create("concord.flow.defs");

    public FlowDefinitionReference(YAMLScalar element) {
        super(element, TextRange.allOf(element.getText()), false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        var el = getElement();

        var flowDefs = CachedValuesManager.getCachedValue(el, FLOW_DEFS_CACHE, () -> {
            var resolved = resolveAll(el);

            return CachedValueProvider.Result.create(
                    resolved,
                    PsiModificationTracker.MODIFICATION_COUNT,
                    el
            );
        });

        if (flowDefs.isEmpty()) {
            return ResolveResult.EMPTY_ARRAY;
        }
        return PsiElementResolveResult.createResults(flowDefs.toArray(PsiElement[]::new));
    }

    private static List<PsiElement> resolveAll(@NotNull YAMLScalar callElement) {
        var process = ProcessDefinitionProvider.getInstance().get(callElement);
        if (process == null) {
            return List.of();
        }

        var flowName = callElement.getTextValue();
        return process.flows(flowName);
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) {
        var el = getElement();
        var generator = brig.concord.yaml.YAMLElementGenerator.getInstance(el.getProject());
        var newElement = generator.createYamlScalar(newElementName);
        return el.replace(newElement);
    }
}

