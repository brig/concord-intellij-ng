// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.*;

import java.util.Collection;
import java.util.Collections;

public class YAMLSequenceItemImpl extends YAMLPsiElementImpl implements YAMLSequenceItem {
    public YAMLSequenceItemImpl(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable YAMLValue getValue() {
        return PsiTreeUtil.findChildOfType(this, YAMLValue.class);
    }

    @Override
    public @NotNull Collection<YAMLKeyValue> getKeysValues() {
        final YAMLMapping mapping = PsiTreeUtil.findChildOfType(this, YAMLMapping.class);
        if (mapping == null) {
            return Collections.emptyList();
        }
        else {
            return mapping.getKeyValues();
        }
    }

    @Override
    public String toString() {
        return "YAML sequence item";
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor)visitor).visitSequenceItem(this);
        }
        else {
            super.accept(visitor);
        }
    }

    @Override
    public int getItemIndex() {
        PsiElement parent = getParent();
        if (parent instanceof YAMLSequence) {
            return ((YAMLSequence)parent).getItems().indexOf(this);
        }
        return 0;
    }
}
