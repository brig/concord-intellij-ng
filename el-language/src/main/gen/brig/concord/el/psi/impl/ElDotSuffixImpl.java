/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 Concord Plugin Authors
 */

// This is a generated file. Not intended for manual editing.
package brig.concord.el.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static brig.concord.el.psi.ElTypes.*;
import brig.concord.el.psi.*;

public class ElDotSuffixImpl extends ElSuffixImpl implements ElDotSuffix {

  public ElDotSuffixImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ElVisitor visitor) {
    visitor.visitDotSuffix(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ElVisitor) accept((ElVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ElArgList getArgList() {
    return findChildByClass(ElArgList.class);
  }

  @Override
  @Nullable
  public ElMemberName getMemberName() {
    return findChildByClass(ElMemberName.class);
  }

}
