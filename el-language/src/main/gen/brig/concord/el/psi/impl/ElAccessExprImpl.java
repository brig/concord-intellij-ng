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

public class ElAccessExprImpl extends ElExpressionImpl implements ElAccessExpr {

  public ElAccessExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ElVisitor visitor) {
    visitor.visitAccessExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ElVisitor) accept((ElVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ElExpression getExpression() {
    return findNotNullChildByClass(ElExpression.class);
  }

  @Override
  @NotNull
  public List<ElSuffix> getSuffixList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ElSuffix.class);
  }

}
