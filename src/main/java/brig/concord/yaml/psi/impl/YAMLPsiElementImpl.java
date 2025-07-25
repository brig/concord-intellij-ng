package brig.concord.yaml.psi.impl;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.YAMLPsiElement;

public class YAMLPsiElementImpl extends ASTWrapperPsiElement implements YAMLPsiElement {
    public YAMLPsiElementImpl(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    public String toString() {
        return "YAML element";
    }
}
