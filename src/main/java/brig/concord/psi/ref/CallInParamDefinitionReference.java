// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.psi.ref;

import brig.concord.completion.provider.FlowCallParamsProvider;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLKeyValue;

public class CallInParamDefinitionReference extends PsiReferenceBase.Poly<YAMLKeyValue> implements PsiPolyVariantReference {

    private static final Key<CachedValue<PsiElement>> IN_PARAM_DEF_CACHE = Key.create("concord.call.in.param.def");

    public CallInParamDefinitionReference(YAMLKeyValue element) {
        super(element,
                element.getKey() != null ? element.getKey().getTextRangeInParent()
                        : TextRange.allOf(element.getKeyText()),
                false);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        var el = getElement();

        var inParamDef = CachedValuesManager.getCachedValue(el, IN_PARAM_DEF_CACHE, () -> {
            var callKv = FlowCallParamsProvider.findCallKv(el);

            if (callKv != null) {
                var resolved = FlowCallParamsProvider.getInstance().inParamDefinition(el);
                return CachedValueProvider.Result.create(
                        resolved,
                        PsiModificationTracker.MODIFICATION_COUNT,
                        el,
                        callKv
                );
            } else {
                return CachedValueProvider.Result.create(
                        null,
                        PsiModificationTracker.MODIFICATION_COUNT,
                        el
                );
            }
        });

        return inParamDef == null ? ResolveResult.EMPTY_ARRAY : PsiElementResolveResult.createResults(inParamDef);
    }
}

