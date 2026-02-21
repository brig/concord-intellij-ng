// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi.impl.yaml;

import brig.concord.psi.impl.delegate.ConcordYamlDelegateFactory;
import brig.concord.yaml.psi.impl.YAMLQuotedTextImpl;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class YAMLConcordQuotedTextImpl extends YAMLQuotedTextImpl implements PsiNamedElement {

    private transient PsiNamedElement delegate;

    public YAMLConcordQuotedTextImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return getDelegate().getReferences();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        return getDelegate().setName(name);
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public void delete() throws IncorrectOperationException {
        getDelegate().delete();
    }

    private PsiNamedElement getDelegate() {
        if (delegate == null) {
            delegate = ConcordYamlDelegateFactory.createDelegate(this);
        }
        return delegate;
    }
}
