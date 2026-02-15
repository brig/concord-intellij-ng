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

public class ElConcatExprImpl extends ASTWrapperPsiElement implements ElConcatExpr {

  public ElConcatExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ElVisitor visitor) {
    visitor.visitConcatExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ElVisitor) accept((ElVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ElAddExpr> getAddExprList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ElAddExpr.class);
  }

}
