// This is a generated file. Not intended for manual editing.
package brig.concord.el.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class ElVisitor extends PsiElementVisitor {

  public void visitAccessExpr(@NotNull ElAccessExpr o) {
    visitExpression(o);
  }

  public void visitAddExpr(@NotNull ElAddExpr o) {
    visitExpression(o);
  }

  public void visitAndExpr(@NotNull ElAndExpr o) {
    visitExpression(o);
  }

  public void visitArgList(@NotNull ElArgList o) {
    visitPsiElement(o);
  }

  public void visitAssignExpr(@NotNull ElAssignExpr o) {
    visitExpression(o);
  }

  public void visitBracketSuffix(@NotNull ElBracketSuffix o) {
    visitSuffix(o);
  }

  public void visitCallSuffix(@NotNull ElCallSuffix o) {
    visitSuffix(o);
  }

  public void visitChoiceExpr(@NotNull ElChoiceExpr o) {
    visitExpression(o);
  }

  public void visitConcatExpr(@NotNull ElConcatExpr o) {
    visitExpression(o);
  }

  public void visitDotSuffix(@NotNull ElDotSuffix o) {
    visitSuffix(o);
  }

  public void visitEqExpr(@NotNull ElEqExpr o) {
    visitExpression(o);
  }

  public void visitExpression(@NotNull ElExpression o) {
    visitPsiElement(o);
  }

  public void visitFunctionExpr(@NotNull ElFunctionExpr o) {
    visitExpression(o);
  }

  public void visitIdentifierExpr(@NotNull ElIdentifierExpr o) {
    visitExpression(o);
  }

  public void visitListLiteral(@NotNull ElListLiteral o) {
    visitExpression(o);
  }

  public void visitLiteral(@NotNull ElLiteral o) {
    visitExpression(o);
  }

  public void visitMapEntry(@NotNull ElMapEntry o) {
    visitPsiElement(o);
  }

  public void visitMapLiteral(@NotNull ElMapLiteral o) {
    visitExpression(o);
  }

  public void visitMemberName(@NotNull ElMemberName o) {
    visitPsiElement(o);
  }

  public void visitMulExpr(@NotNull ElMulExpr o) {
    visitExpression(o);
  }

  public void visitOrExpr(@NotNull ElOrExpr o) {
    visitExpression(o);
  }

  public void visitParenExpr(@NotNull ElParenExpr o) {
    visitExpression(o);
  }

  public void visitPrefixExpr(@NotNull ElPrefixExpr o) {
    visitExpression(o);
  }

  public void visitPrimaryExpr(@NotNull ElPrimaryExpr o) {
    visitExpression(o);
  }

  public void visitRelExpr(@NotNull ElRelExpr o) {
    visitExpression(o);
  }

  public void visitStringLiteral(@NotNull ElStringLiteral o) {
    visitLiteral(o);
  }

  public void visitSuffix(@NotNull ElSuffix o) {
    visitPsiElement(o);
  }

  public void visitUnaryExpr(@NotNull ElUnaryExpr o) {
    visitExpression(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
