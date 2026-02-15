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

public class ElPrimaryExprImpl extends ASTWrapperPsiElement implements ElPrimaryExpr {

  public ElPrimaryExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ElVisitor visitor) {
    visitor.visitPrimaryExpr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ElVisitor) accept((ElVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ElFunctionExpr getFunctionExpr() {
    return findChildByClass(ElFunctionExpr.class);
  }

  @Override
  @Nullable
  public ElIdentifierExpr getIdentifierExpr() {
    return findChildByClass(ElIdentifierExpr.class);
  }

  @Override
  @Nullable
  public ElListLiteral getListLiteral() {
    return findChildByClass(ElListLiteral.class);
  }

  @Override
  @Nullable
  public ElLiteral getLiteral() {
    return findChildByClass(ElLiteral.class);
  }

  @Override
  @Nullable
  public ElMapLiteral getMapLiteral() {
    return findChildByClass(ElMapLiteral.class);
  }

  @Override
  @Nullable
  public ElParenExpr getParenExpr() {
    return findChildByClass(ElParenExpr.class);
  }

}
