// This is a generated file. Not intended for manual editing.
package brig.concord.el.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static brig.concord.el.psi.ElTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import brig.concord.el.psi.*;

public class ElAssignExprImpl extends ASTWrapperPsiElement implements ElAssignExpr {

  public ElAssignExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ElVisitor visitor) {
    visitor.visitAssignExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ElVisitor) accept((ElVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ElAssignExpr getAssignExpr() {
    return findChildByClass(ElAssignExpr.class);
  }

  @Override
  @NotNull
  public ElChoiceExpr getChoiceExpr() {
    return findNotNullChildByClass(ElChoiceExpr.class);
  }

}
