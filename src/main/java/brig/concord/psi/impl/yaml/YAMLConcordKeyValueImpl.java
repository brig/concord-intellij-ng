// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi.impl.yaml;

import brig.concord.psi.impl.delegate.ConcordYamlDelegateFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.YAMLUtil;
import brig.concord.yaml.psi.impl.YAMLKeyValueImpl;

public class YAMLConcordKeyValueImpl extends YAMLKeyValueImpl {

    public YAMLConcordKeyValueImpl(@NotNull ASTNode node) {
        super(node);
    }

    private PsiNamedElement getDelegate() {
        return ConcordYamlDelegateFactory.createDelegate(this);
    }

    @Override
    public PsiReference getReference() {
        return getDelegate().getReference();
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return getDelegate().getReferences();
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        return getDelegate().getUseScope();
    }

    @Override
    public void delete() throws IncorrectOperationException {
        getDelegate().delete();
    }

    @Override
    public PsiElement setName(@NonNls @NotNull String newName) throws IncorrectOperationException {
        return YAMLUtil.rename(this, newName);
    }
}
