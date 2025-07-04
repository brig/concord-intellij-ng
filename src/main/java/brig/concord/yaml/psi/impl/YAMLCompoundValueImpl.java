package brig.concord.yaml.psi.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLCompoundValue;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YamlPsiElementVisitor;

public class YAMLCompoundValueImpl extends YAMLValueImpl implements YAMLCompoundValue {
    public YAMLCompoundValueImpl(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML compound value";
    }

    @Override
    public @NotNull String getTextValue() {
        PsiElement element = getTag() != null ? getTag().getNextSibling() : getFirstChild();

        while (element != null && !(element instanceof YAMLScalar)) {
            element = element.getNextSibling();
        }

        if (element != null) {
            return ((YAMLScalar)element).getTextValue();
        }
        else {
            return "<compoundValue:" + Integer.toHexString(getText().hashCode()) + ">";
        }
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor)visitor).visitCompoundValue(this);
        }
        else {
            super.accept(visitor);
        }
    }
}
