// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.psi.YAMLValue;
import brig.concord.yaml.psi.YamlPsiElementVisitor;

public abstract class YAMLValueImpl extends YAMLPsiElementImpl implements YAMLValue {
    YAMLValueImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PsiElement getTag() {
        final PsiElement firstChild = getFirstChild();
        if (firstChild.getNode().getElementType() == YAMLTokenTypes.TAG) {
            return firstChild;
        }
        else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "YAML value";
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor)visitor).visitValue(this);
        }
        else {
            super.accept(visitor);
        }
    }
}
