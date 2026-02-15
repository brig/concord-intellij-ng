// This is a generated file. Not intended for manual editing.
package brig.concord.el.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ElPrimaryExpr extends PsiElement {

  @Nullable
  ElFunctionExpr getFunctionExpr();

  @Nullable
  ElIdentifierExpr getIdentifierExpr();

  @Nullable
  ElListLiteral getListLiteral();

  @Nullable
  ElLiteral getLiteral();

  @Nullable
  ElMapLiteral getMapLiteral();

  @Nullable
  ElParenExpr getParenExpr();

}
