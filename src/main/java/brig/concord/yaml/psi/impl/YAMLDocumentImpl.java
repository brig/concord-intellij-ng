package brig.concord.yaml.psi.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.ConcordBundle;
import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLValue;
import brig.concord.yaml.psi.YamlPsiElementVisitor;

import javax.swing.*;

public class YAMLDocumentImpl extends YAMLPsiElementImpl implements YAMLDocument {
    public YAMLDocumentImpl(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML document";
    }

    @Override
    public @Nullable YAMLValue getTopLevelValue() {
        return PsiTreeUtil.findChildOfType(this, YAMLValue.class);
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor)visitor).visitDocument(this);
        }
        else {
            super.accept(visitor);
        }
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public @NotNull String getPresentableText() {
                return ConcordBundle.message("element.presentation.document.type");
            }

            @Override
            public @NotNull String getLocationString() {
                return getContainingFile().getName();
            }

            @Override
            public @NotNull Icon getIcon(boolean unused) {
                return AllIcons.Json.Object;
            }
        };
    }
}
