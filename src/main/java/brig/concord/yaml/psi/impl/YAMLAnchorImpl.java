// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLAnchor;
import brig.concord.yaml.psi.YAMLValue;
import brig.concord.yaml.psi.YamlPsiElementVisitor;

/** Current implementation  consists of 2 nodes: ampersand symbol and name identifier */
public class YAMLAnchorImpl extends YAMLPsiElementImpl implements YAMLAnchor {
    public YAMLAnchorImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull String getName() {
        return getNameIdentifier().getText();
    }

    @Override
    public @NotNull PsiElement getNameIdentifier() {
        return getLastChild();
    }

    @Override
    public @NotNull PsiElement getNavigationElement() {
        return getNameIdentifier();
    }

    @Override
    public int getTextOffset() {
        return getNavigationElement().getNode().getStartOffset();
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        PsiElement nameIdentifier = getNameIdentifier();
        assert nameIdentifier instanceof LeafPsiElement;

        ((LeafPsiElement)nameIdentifier).replaceWithText(name);

        return this;
    }

    @Override
    public @Nullable YAMLValue getMarkedValue() {
        PsiElement parent = getParent();
        if (parent instanceof YAMLValue) {
            return (YAMLValue)parent;
        }
        return null;
    }

    @Override
    public String toString() {
        return "YAML anchor";
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor)visitor).visitAnchor(this);
        }
        else {
            super.accept(visitor);
        }
    }
}

