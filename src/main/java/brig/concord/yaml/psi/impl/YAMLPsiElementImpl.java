// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi.impl;

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
