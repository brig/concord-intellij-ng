package brig.concord.psi.impl.yaml;

import brig.concord.psi.impl.delegate.ConcordYamlDelegateFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLUtil;
import org.jetbrains.yaml.psi.impl.YAMLKeyValueImpl;

import java.util.Collection;

public class YAMLConcordKeyValueImpl extends YAMLKeyValueImpl {

    public YAMLConcordKeyValueImpl(@NotNull ASTNode node) {
        super(node);
    }

    private transient PsiNamedElement delegate;

    private PsiNamedElement getDelegate() {
        if (delegate == null) {
            delegate = ConcordYamlDelegateFactory.createDelegate(this);
        }
        return delegate;
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
        Collection<PsiReference> referenceCollection =
                ReferencesSearch.search(this, getUseScope()).findAll();
        PsiElement rename = YAMLUtil.rename(this, newName);
        referenceCollection.forEach(reference -> reference.handleElementRename(newName));
        return rename;
    }

    @Override
    public @Nullable String getName() {
        return super.getName();
    }
}
