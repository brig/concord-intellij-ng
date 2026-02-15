// This is a generated file. Not intended for manual editing.
package brig.concord.el.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import brig.concord.el.psi.impl.*;

public interface ElTypes {

  IElementType ACCESS_EXPR = new ElElementType("ACCESS_EXPR");
  IElementType ADD_EXPR = new ElElementType("ADD_EXPR");
  IElementType AND_EXPR = new ElElementType("AND_EXPR");
  IElementType ARG_LIST = new ElElementType("ARG_LIST");
  IElementType ASSIGN_EXPR = new ElElementType("ASSIGN_EXPR");
  IElementType BRACKET_SUFFIX = new ElElementType("BRACKET_SUFFIX");
  IElementType CALL_SUFFIX = new ElElementType("CALL_SUFFIX");
  IElementType CHOICE_EXPR = new ElElementType("CHOICE_EXPR");
  IElementType CONCAT_EXPR = new ElElementType("CONCAT_EXPR");
  IElementType DOT_SUFFIX = new ElElementType("DOT_SUFFIX");
  IElementType EQ_EXPR = new ElElementType("EQ_EXPR");
  IElementType EXPRESSION = new ElElementType("EXPRESSION");
  IElementType FUNCTION_EXPR = new ElElementType("FUNCTION_EXPR");
  IElementType IDENTIFIER_EXPR = new ElElementType("IDENTIFIER_EXPR");
  IElementType LIST_LITERAL = new ElElementType("LIST_LITERAL");
  IElementType LITERAL = new ElElementType("LITERAL");
  IElementType MAP_ENTRY = new ElElementType("MAP_ENTRY");
  IElementType MAP_LITERAL = new ElElementType("MAP_LITERAL");
  IElementType MEMBER_NAME = new ElElementType("MEMBER_NAME");
  IElementType MUL_EXPR = new ElElementType("MUL_EXPR");
  IElementType OR_EXPR = new ElElementType("OR_EXPR");
  IElementType PAREN_EXPR = new ElElementType("PAREN_EXPR");
  IElementType PREFIX_EXPR = new ElElementType("PREFIX_EXPR");
  IElementType PRIMARY_EXPR = new ElElementType("PRIMARY_EXPR");
  IElementType REL_EXPR = new ElElementType("REL_EXPR");
  IElementType STRING_LITERAL = new ElElementType("STRING_LITERAL");
  IElementType SUFFIX = new ElElementType("SUFFIX");
  IElementType UNARY_EXPR = new ElElementType("UNARY_EXPR");

  IElementType AND_KEYWORD = new ElTokenType("and");
  IElementType AND_OP = new ElTokenType("&&");
  IElementType ARROW = new ElTokenType("->");
  IElementType ASSIGN = new ElTokenType("=");
  IElementType COLON = new ElTokenType(":");
  IElementType COMMA = new ElTokenType(",");
  IElementType CONCAT = new ElTokenType("+=");
  IElementType DIV_KEYWORD = new ElTokenType("div");
  IElementType DIV_OP = new ElTokenType("/");
  IElementType DOT = new ElTokenType(".");
  IElementType DOUBLE_QUOTED_STRING = new ElTokenType("DOUBLE_QUOTED_STRING");
  IElementType EMPTY_KEYWORD = new ElTokenType("empty");
  IElementType EQ_KEYWORD = new ElTokenType("eq");
  IElementType EQ_OP = new ElTokenType("==");
  IElementType FALSE_KEYWORD = new ElTokenType("false");
  IElementType FLOAT_LITERAL = new ElTokenType("FLOAT_LITERAL");
  IElementType GE_KEYWORD = new ElTokenType("ge");
  IElementType GE_OP = new ElTokenType(">=");
  IElementType GT_KEYWORD = new ElTokenType("gt");
  IElementType GT_OP = new ElTokenType(">");
  IElementType IDENTIFIER = new ElTokenType("IDENTIFIER");
  IElementType INSTANCEOF_KEYWORD = new ElTokenType("instanceof");
  IElementType INTEGER_LITERAL = new ElTokenType("INTEGER_LITERAL");
  IElementType LBRACE = new ElTokenType("{");
  IElementType LBRACKET = new ElTokenType("[");
  IElementType LE_KEYWORD = new ElTokenType("le");
  IElementType LE_OP = new ElTokenType("<=");
  IElementType LPAREN = new ElTokenType("(");
  IElementType LT_KEYWORD = new ElTokenType("lt");
  IElementType LT_OP = new ElTokenType("<");
  IElementType MINUS = new ElTokenType("-");
  IElementType MOD_KEYWORD = new ElTokenType("mod");
  IElementType MOD_OP = new ElTokenType("%");
  IElementType MULT = new ElTokenType("*");
  IElementType NE_KEYWORD = new ElTokenType("ne");
  IElementType NE_OP = new ElTokenType("!=");
  IElementType NOT_KEYWORD = new ElTokenType("not");
  IElementType NOT_OP = new ElTokenType("!");
  IElementType NULL_KEYWORD = new ElTokenType("null");
  IElementType OR_KEYWORD = new ElTokenType("or");
  IElementType OR_OP = new ElTokenType("||");
  IElementType PLUS = new ElTokenType("+");
  IElementType QUESTION = new ElTokenType("?");
  IElementType RBRACE = new ElTokenType("}");
  IElementType RBRACKET = new ElTokenType("]");
  IElementType RPAREN = new ElTokenType(")");
  IElementType SEMICOLON = new ElTokenType(";");
  IElementType SINGLE_QUOTED_STRING = new ElTokenType("SINGLE_QUOTED_STRING");
  IElementType TRUE_KEYWORD = new ElTokenType("true");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == ACCESS_EXPR) {
        return new ElAccessExprImpl(node);
      }
      else if (type == ADD_EXPR) {
        return new ElAddExprImpl(node);
      }
      else if (type == AND_EXPR) {
        return new ElAndExprImpl(node);
      }
      else if (type == ARG_LIST) {
        return new ElArgListImpl(node);
      }
      else if (type == ASSIGN_EXPR) {
        return new ElAssignExprImpl(node);
      }
      else if (type == BRACKET_SUFFIX) {
        return new ElBracketSuffixImpl(node);
      }
      else if (type == CALL_SUFFIX) {
        return new ElCallSuffixImpl(node);
      }
      else if (type == CHOICE_EXPR) {
        return new ElChoiceExprImpl(node);
      }
      else if (type == CONCAT_EXPR) {
        return new ElConcatExprImpl(node);
      }
      else if (type == DOT_SUFFIX) {
        return new ElDotSuffixImpl(node);
      }
      else if (type == EQ_EXPR) {
        return new ElEqExprImpl(node);
      }
      else if (type == EXPRESSION) {
        return new ElExpressionImpl(node);
      }
      else if (type == FUNCTION_EXPR) {
        return new ElFunctionExprImpl(node);
      }
      else if (type == IDENTIFIER_EXPR) {
        return new ElIdentifierExprImpl(node);
      }
      else if (type == LIST_LITERAL) {
        return new ElListLiteralImpl(node);
      }
      else if (type == LITERAL) {
        return new ElLiteralImpl(node);
      }
      else if (type == MAP_ENTRY) {
        return new ElMapEntryImpl(node);
      }
      else if (type == MAP_LITERAL) {
        return new ElMapLiteralImpl(node);
      }
      else if (type == MEMBER_NAME) {
        return new ElMemberNameImpl(node);
      }
      else if (type == MUL_EXPR) {
        return new ElMulExprImpl(node);
      }
      else if (type == OR_EXPR) {
        return new ElOrExprImpl(node);
      }
      else if (type == PAREN_EXPR) {
        return new ElParenExprImpl(node);
      }
      else if (type == PREFIX_EXPR) {
        return new ElPrefixExprImpl(node);
      }
      else if (type == PRIMARY_EXPR) {
        return new ElPrimaryExprImpl(node);
      }
      else if (type == REL_EXPR) {
        return new ElRelExprImpl(node);
      }
      else if (type == STRING_LITERAL) {
        return new ElStringLiteralImpl(node);
      }
      else if (type == SUFFIX) {
        return new ElSuffixImpl(node);
      }
      else if (type == UNARY_EXPR) {
        return new ElUnaryExprImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
