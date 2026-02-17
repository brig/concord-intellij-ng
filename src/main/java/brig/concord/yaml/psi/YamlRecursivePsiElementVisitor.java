// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;
import org.jetbrains.annotations.NotNull;

public abstract class YamlRecursivePsiElementVisitor extends YamlPsiElementVisitor implements PsiRecursiveVisitor {
    @Override
    public void visitElement(@NotNull PsiElement element) {
        ProgressManager.checkCanceled();
        element.acceptChildren(this);
    }
}
