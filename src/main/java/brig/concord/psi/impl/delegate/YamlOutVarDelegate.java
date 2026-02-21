// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi.impl.delegate;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import brig.concord.yaml.psi.YAMLScalar;
import org.jetbrains.annotations.NotNull;

/**
 * Delegate that provides {@link PsiNamedElement} support for scalar out-variable
 * declarations (e.g., {@code out: result}). Returns the scalar text value as
 * the element name, enabling {@code ReferencesSearch} (Find Usages) to work.
 */
class YamlOutVarDelegate extends ASTWrapperPsiElement implements PsiNamedElement {

    private final YAMLScalar scalar;

    YamlOutVarDelegate(@NotNull ASTNode node, @NotNull YAMLScalar scalar) {
        super(node);
        this.scalar = scalar;
    }

    @Override
    public String getName() {
        return scalar.getTextValue().trim();
    }

    @Override
    public PsiElement setName(@NlsSafe @NotNull String name) throws IncorrectOperationException {
        throw new IncorrectOperationException("Rename not supported for out variable scalars");
    }
}