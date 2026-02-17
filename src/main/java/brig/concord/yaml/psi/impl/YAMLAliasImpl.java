// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi.impl;


import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLAlias;
import brig.concord.yaml.psi.YamlPsiElementVisitor;
import brig.concord.yaml.resolve.YAMLAliasReference;

/** Current implementation consists of 2 nodes: star symbol and name identifier */
public class YAMLAliasImpl extends YAMLValueImpl implements YAMLAlias {
    public YAMLAliasImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @NotNull String getAliasName() {
        LeafPsiElement identifier = getIdentifierPsi();
        return identifier == null ? "" : identifier.getText();
    }

    @Override
    public YAMLAliasReference getReference() {
        return getIdentifierPsi() == null ? null : new YAMLAliasReference(this);
    }

    @Override
    public String toString() {
        return "YAML alias";
    }

    /** For now it could not return null but better do not rely on it */
    @Contract(pure = true)
    public @Nullable LeafPsiElement getIdentifierPsi() {
        return (LeafPsiElement)getLastChild();
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor)visitor).visitAlias(this);
        }
        else {
            super.accept(visitor);
        }
    }
}
