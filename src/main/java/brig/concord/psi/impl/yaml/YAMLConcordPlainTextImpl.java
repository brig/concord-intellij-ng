// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.psi.impl.yaml;

import brig.concord.psi.impl.delegate.ConcordYamlDelegateFactory;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.impl.YAMLPlainTextImpl;

public class YAMLConcordPlainTextImpl extends YAMLPlainTextImpl implements PsiNamedElement {

    private transient PsiNamedElement delegate;

    public YAMLConcordPlainTextImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull SearchScope getUseScope() {
        PsiFile containingFile = getContainingFile();
        return containingFile != null ? GlobalSearchScope.fileScope(containingFile) : GlobalSearchScope.EMPTY_SCOPE;
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return getDelegate().getReferences();
    }

    @Override
    public PsiElement setName(@NlsSafe @NotNull String name) throws IncorrectOperationException {
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
