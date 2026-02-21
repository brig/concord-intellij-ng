// SPDX-License-Identifier: Apache-2.0
package brig.concord.el.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import org.jetbrains.annotations.NotNull;

public abstract class ElIdentifierExprMixin extends ElExpressionImpl {

    public ElIdentifierExprMixin(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return ReferenceProvidersRegistry.getReferencesFromProviders(this);
    }

    @Override
    public PsiReference getReference() {
        var refs = getReferences();
        return refs.length > 0 ? refs[0] : null;
    }
}