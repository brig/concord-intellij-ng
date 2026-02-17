/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 Concord Plugin Authors
 */

// This is a generated file. Not intended for manual editing.
package brig.concord.el.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static brig.concord.el.psi.ElTypes.*;
import static brig.concord.el.parser.ElParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ElParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType root_, PsiBuilder builder_) {
    parseLight(root_, builder_);
    return builder_.getTreeBuilt();
  }

  public void parseLight(IElementType root_, PsiBuilder builder_) {
    boolean result_;
    builder_ = adapt_builder_(root_, builder_, this, EXTENDS_SETS_);
    Marker marker_ = enter_section_(builder_, 0, _COLLAPSE_, null);
    result_ = parse_root_(root_, builder_);
    exit_section_(builder_, 0, marker_, root_, result_, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType root_, PsiBuilder builder_) {
    return parse_root_(root_, builder_, 0);
  }

  static boolean parse_root_(IElementType root_, PsiBuilder builder_, int level_) {
    return root(builder_, level_ + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(BRACKET_SUFFIX, CALL_SUFFIX, DOT_SUFFIX, SUFFIX),
    create_token_set_(ACCESS_EXPR, ADD_EXPR, AND_EXPR, ASSIGN_EXPR,
      CHOICE_EXPR, CONCAT_EXPR, EQ_EXPR, EXPRESSION,
      FUNCTION_EXPR, IDENTIFIER_EXPR, LIST_LITERAL, LITERAL,
      MAP_LITERAL, MUL_EXPR, OR_EXPR, PAREN_EXPR,
      PREFIX_EXPR, PRIMARY_EXPR, REL_EXPR, STRING_LITERAL,
      UNARY_EXPR),
  };

  /* ********************************************************** */
  // primaryExpr suffix*
  public static boolean accessExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "accessExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, ACCESS_EXPR, "<access expr>");
    result_ = primaryExpr(builder_, level_ + 1);
    result_ = result_ && accessExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // suffix*
  private static boolean accessExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "accessExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!suffix(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "accessExpr_1", pos_)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // mulExpr ((PLUS | MINUS) mulExpr)*
  public static boolean addExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "addExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, ADD_EXPR, "<add expr>");
    result_ = mulExpr(builder_, level_ + 1);
    result_ = result_ && addExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((PLUS | MINUS) mulExpr)*
  private static boolean addExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "addExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!addExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "addExpr_1", pos_)) break;
    }
    return true;
  }

  // (PLUS | MINUS) mulExpr
  private static boolean addExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "addExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = addExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && mulExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // PLUS | MINUS
  private static boolean addExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "addExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, PLUS);
    if (!result_) result_ = consumeToken(builder_, MINUS);
    return result_;
  }

  /* ********************************************************** */
  // eqExpr ((AND_OP | AND_KEYWORD) eqExpr)*
  public static boolean andExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "andExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, AND_EXPR, "<and expr>");
    result_ = eqExpr(builder_, level_ + 1);
    result_ = result_ && andExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((AND_OP | AND_KEYWORD) eqExpr)*
  private static boolean andExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "andExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!andExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "andExpr_1", pos_)) break;
    }
    return true;
  }

  // (AND_OP | AND_KEYWORD) eqExpr
  private static boolean andExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "andExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = andExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && eqExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // AND_OP | AND_KEYWORD
  private static boolean andExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "andExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, AND_OP);
    if (!result_) result_ = consumeToken(builder_, AND_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // LPAREN [expression (COMMA expression)*] RPAREN
  public static boolean argList(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argList")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, ARG_LIST, null);
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, argList_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [expression (COMMA expression)*]
  private static boolean argList_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argList_1")) return false;
    argList_1_0(builder_, level_ + 1);
    return true;
  }

  // expression (COMMA expression)*
  private static boolean argList_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argList_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && argList_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA expression)*
  private static boolean argList_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argList_1_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!argList_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "argList_1_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean argList_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "argList_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // choiceExpr (ASSIGN assignExpr | ARROW assignExpr)?
  public static boolean assignExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, ASSIGN_EXPR, "<assign expr>");
    result_ = choiceExpr(builder_, level_ + 1);
    result_ = result_ && assignExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (ASSIGN assignExpr | ARROW assignExpr)?
  private static boolean assignExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignExpr_1")) return false;
    assignExpr_1_0(builder_, level_ + 1);
    return true;
  }

  // ASSIGN assignExpr | ARROW assignExpr
  private static boolean assignExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = assignExpr_1_0_0(builder_, level_ + 1);
    if (!result_) result_ = assignExpr_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ASSIGN assignExpr
  private static boolean assignExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignExpr_1_0_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ASSIGN);
    result_ = result_ && assignExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // ARROW assignExpr
  private static boolean assignExpr_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "assignExpr_1_0_1")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, ARROW);
    result_ = result_ && assignExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACKET expression RBRACKET argList?
  public static boolean bracketSuffix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bracketSuffix")) return false;
    if (!nextTokenIs(builder_, LBRACKET)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, BRACKET_SUFFIX, null);
    result_ = consumeToken(builder_, LBRACKET);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, expression(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, consumeToken(builder_, RBRACKET)) && result_;
    result_ = pinned_ && bracketSuffix_3(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // argList?
  private static boolean bracketSuffix_3(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "bracketSuffix_3")) return false;
    argList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // argList
  public static boolean callSuffix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "callSuffix")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = argList(builder_, level_ + 1);
    exit_section_(builder_, marker_, CALL_SUFFIX, result_);
    return result_;
  }

  /* ********************************************************** */
  // orExpr (QUESTION choiceExpr COLON choiceExpr)?
  public static boolean choiceExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "choiceExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, CHOICE_EXPR, "<choice expr>");
    result_ = orExpr(builder_, level_ + 1);
    result_ = result_ && choiceExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (QUESTION choiceExpr COLON choiceExpr)?
  private static boolean choiceExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "choiceExpr_1")) return false;
    choiceExpr_1_0(builder_, level_ + 1);
    return true;
  }

  // QUESTION choiceExpr COLON choiceExpr
  private static boolean choiceExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "choiceExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, QUESTION);
    result_ = result_ && choiceExpr(builder_, level_ + 1);
    result_ = result_ && consumeToken(builder_, COLON);
    result_ = result_ && choiceExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // addExpr (CONCAT addExpr)*
  public static boolean concatExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "concatExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, CONCAT_EXPR, "<concat expr>");
    result_ = addExpr(builder_, level_ + 1);
    result_ = result_ && concatExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (CONCAT addExpr)*
  private static boolean concatExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "concatExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!concatExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "concatExpr_1", pos_)) break;
    }
    return true;
  }

  // CONCAT addExpr
  private static boolean concatExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "concatExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, CONCAT);
    result_ = result_ && addExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // DOT memberName argList?
  public static boolean dotSuffix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "dotSuffix")) return false;
    if (!nextTokenIs(builder_, DOT)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, DOT_SUFFIX, null);
    result_ = consumeToken(builder_, DOT);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, memberName(builder_, level_ + 1));
    result_ = pinned_ && dotSuffix_2(builder_, level_ + 1) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // argList?
  private static boolean dotSuffix_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "dotSuffix_2")) return false;
    argList(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // relExpr ((EQ_OP | EQ_KEYWORD | NE_OP | NE_KEYWORD) relExpr)*
  public static boolean eqExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "eqExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EQ_EXPR, "<eq expr>");
    result_ = relExpr(builder_, level_ + 1);
    result_ = result_ && eqExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((EQ_OP | EQ_KEYWORD | NE_OP | NE_KEYWORD) relExpr)*
  private static boolean eqExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "eqExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!eqExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "eqExpr_1", pos_)) break;
    }
    return true;
  }

  // (EQ_OP | EQ_KEYWORD | NE_OP | NE_KEYWORD) relExpr
  private static boolean eqExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "eqExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = eqExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && relExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // EQ_OP | EQ_KEYWORD | NE_OP | NE_KEYWORD
  private static boolean eqExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "eqExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, EQ_OP);
    if (!result_) result_ = consumeToken(builder_, EQ_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, NE_OP);
    if (!result_) result_ = consumeToken(builder_, NE_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // assignExpr (SEMICOLON assignExpr)*
  public static boolean expression(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, EXPRESSION, "<expression>");
    result_ = assignExpr(builder_, level_ + 1);
    result_ = result_ && expression_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (SEMICOLON assignExpr)*
  private static boolean expression_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!expression_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "expression_1", pos_)) break;
    }
    return true;
  }

  // SEMICOLON assignExpr
  private static boolean expression_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "expression_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, SEMICOLON);
    result_ = result_ && assignExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // <<isFunctionCall>> IDENTIFIER COLON IDENTIFIER argList+
  public static boolean functionExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionExpr")) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, FUNCTION_EXPR, "<function expr>");
    result_ = isFunctionCall(builder_, level_ + 1);
    result_ = result_ && consumeTokens(builder_, 2, IDENTIFIER, COLON, IDENTIFIER);
    pinned_ = result_; // pin = 3
    result_ = result_ && functionExpr_4(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // argList+
  private static boolean functionExpr_4(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "functionExpr_4")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = argList(builder_, level_ + 1);
    while (result_) {
      int pos_ = current_position_(builder_);
      if (!argList(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "functionExpr_4", pos_)) break;
    }
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER
  public static boolean identifierExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "identifierExpr")) return false;
    if (!nextTokenIs(builder_, IDENTIFIER)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, IDENTIFIER);
    exit_section_(builder_, marker_, IDENTIFIER_EXPR, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACKET [expression (COMMA expression)*] RBRACKET
  public static boolean listLiteral(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "listLiteral")) return false;
    if (!nextTokenIs(builder_, LBRACKET)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, LIST_LITERAL, null);
    result_ = consumeToken(builder_, LBRACKET);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, listLiteral_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACKET) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [expression (COMMA expression)*]
  private static boolean listLiteral_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "listLiteral_1")) return false;
    listLiteral_1_0(builder_, level_ + 1);
    return true;
  }

  // expression (COMMA expression)*
  private static boolean listLiteral_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "listLiteral_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && listLiteral_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA expression)*
  private static boolean listLiteral_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "listLiteral_1_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!listLiteral_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "listLiteral_1_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean listLiteral_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "listLiteral_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // INTEGER_LITERAL
  //   | FLOAT_LITERAL
  //   | stringLiteral
  //   | TRUE_KEYWORD
  //   | FALSE_KEYWORD
  //   | NULL_KEYWORD
  public static boolean literal(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "literal")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, LITERAL, "<literal>");
    result_ = consumeToken(builder_, INTEGER_LITERAL);
    if (!result_) result_ = consumeToken(builder_, FLOAT_LITERAL);
    if (!result_) result_ = stringLiteral(builder_, level_ + 1);
    if (!result_) result_ = consumeToken(builder_, TRUE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, FALSE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, NULL_KEYWORD);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // expression (COLON expression)?
  public static boolean mapEntry(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapEntry")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MAP_ENTRY, "<map entry>");
    result_ = expression(builder_, level_ + 1);
    result_ = result_ && mapEntry_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // (COLON expression)?
  private static boolean mapEntry_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapEntry_1")) return false;
    mapEntry_1_0(builder_, level_ + 1);
    return true;
  }

  // COLON expression
  private static boolean mapEntry_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapEntry_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COLON);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // LBRACE [mapEntry (COMMA mapEntry)*] RBRACE
  public static boolean mapLiteral(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteral")) return false;
    if (!nextTokenIs(builder_, LBRACE)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MAP_LITERAL, null);
    result_ = consumeToken(builder_, LBRACE);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, mapLiteral_1(builder_, level_ + 1));
    result_ = pinned_ && consumeToken(builder_, RBRACE) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // [mapEntry (COMMA mapEntry)*]
  private static boolean mapLiteral_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteral_1")) return false;
    mapLiteral_1_0(builder_, level_ + 1);
    return true;
  }

  // mapEntry (COMMA mapEntry)*
  private static boolean mapLiteral_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteral_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = mapEntry(builder_, level_ + 1);
    result_ = result_ && mapLiteral_1_0_1(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // (COMMA mapEntry)*
  private static boolean mapLiteral_1_0_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteral_1_0_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!mapLiteral_1_0_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "mapLiteral_1_0_1", pos_)) break;
    }
    return true;
  }

  // COMMA mapEntry
  private static boolean mapLiteral_1_0_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mapLiteral_1_0_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && mapEntry(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // IDENTIFIER
  //   | TRUE_KEYWORD | FALSE_KEYWORD | NULL_KEYWORD
  //   | EMPTY_KEYWORD | NOT_KEYWORD | AND_KEYWORD | OR_KEYWORD
  //   | DIV_KEYWORD | MOD_KEYWORD | EQ_KEYWORD | NE_KEYWORD
  //   | LT_KEYWORD | GT_KEYWORD | LE_KEYWORD | GE_KEYWORD
  //   | INSTANCEOF_KEYWORD
  public static boolean memberName(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "memberName")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, MEMBER_NAME, "<member name>");
    result_ = consumeToken(builder_, IDENTIFIER);
    if (!result_) result_ = consumeToken(builder_, TRUE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, FALSE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, NULL_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, EMPTY_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, NOT_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, AND_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, OR_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, DIV_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, MOD_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, EQ_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, NE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, LT_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, GT_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, LE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, GE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, INSTANCEOF_KEYWORD);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // unaryExpr ((MULT | DIV_OP | DIV_KEYWORD | MOD_OP | MOD_KEYWORD) unaryExpr)*
  public static boolean mulExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mulExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, MUL_EXPR, "<mul expr>");
    result_ = unaryExpr(builder_, level_ + 1);
    result_ = result_ && mulExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((MULT | DIV_OP | DIV_KEYWORD | MOD_OP | MOD_KEYWORD) unaryExpr)*
  private static boolean mulExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mulExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!mulExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "mulExpr_1", pos_)) break;
    }
    return true;
  }

  // (MULT | DIV_OP | DIV_KEYWORD | MOD_OP | MOD_KEYWORD) unaryExpr
  private static boolean mulExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mulExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = mulExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && unaryExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // MULT | DIV_OP | DIV_KEYWORD | MOD_OP | MOD_KEYWORD
  private static boolean mulExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "mulExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, MULT);
    if (!result_) result_ = consumeToken(builder_, DIV_OP);
    if (!result_) result_ = consumeToken(builder_, DIV_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, MOD_OP);
    if (!result_) result_ = consumeToken(builder_, MOD_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // andExpr ((OR_OP | OR_KEYWORD) andExpr)*
  public static boolean orExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "orExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, OR_EXPR, "<or expr>");
    result_ = andExpr(builder_, level_ + 1);
    result_ = result_ && orExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((OR_OP | OR_KEYWORD) andExpr)*
  private static boolean orExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "orExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!orExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "orExpr_1", pos_)) break;
    }
    return true;
  }

  // (OR_OP | OR_KEYWORD) andExpr
  private static boolean orExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "orExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = orExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && andExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // OR_OP | OR_KEYWORD
  private static boolean orExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "orExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, OR_OP);
    if (!result_) result_ = consumeToken(builder_, OR_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // LPAREN expression (COMMA expression)* RPAREN
  public static boolean parenExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenExpr")) return false;
    if (!nextTokenIs(builder_, LPAREN)) return false;
    boolean result_, pinned_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, PAREN_EXPR, null);
    result_ = consumeToken(builder_, LPAREN);
    pinned_ = result_; // pin = 1
    result_ = result_ && report_error_(builder_, expression(builder_, level_ + 1));
    result_ = pinned_ && report_error_(builder_, parenExpr_2(builder_, level_ + 1)) && result_;
    result_ = pinned_ && consumeToken(builder_, RPAREN) && result_;
    exit_section_(builder_, level_, marker_, result_, pinned_, null);
    return result_ || pinned_;
  }

  // (COMMA expression)*
  private static boolean parenExpr_2(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenExpr_2")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!parenExpr_2_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "parenExpr_2", pos_)) break;
    }
    return true;
  }

  // COMMA expression
  private static boolean parenExpr_2_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "parenExpr_2_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = consumeToken(builder_, COMMA);
    result_ = result_ && expression(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  /* ********************************************************** */
  // (MINUS | NOT_OP | NOT_KEYWORD | EMPTY_KEYWORD) unaryExpr
  public static boolean prefixExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "prefixExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, PREFIX_EXPR, "<prefix expr>");
    result_ = prefixExpr_0(builder_, level_ + 1);
    result_ = result_ && unaryExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // MINUS | NOT_OP | NOT_KEYWORD | EMPTY_KEYWORD
  private static boolean prefixExpr_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "prefixExpr_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, MINUS);
    if (!result_) result_ = consumeToken(builder_, NOT_OP);
    if (!result_) result_ = consumeToken(builder_, NOT_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, EMPTY_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // literal
  //   | parenExpr
  //   | listLiteral
  //   | mapLiteral
  //   | functionExpr
  //   | identifierExpr
  public static boolean primaryExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "primaryExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, PRIMARY_EXPR, "<primary expr>");
    result_ = literal(builder_, level_ + 1);
    if (!result_) result_ = parenExpr(builder_, level_ + 1);
    if (!result_) result_ = listLiteral(builder_, level_ + 1);
    if (!result_) result_ = mapLiteral(builder_, level_ + 1);
    if (!result_) result_ = functionExpr(builder_, level_ + 1);
    if (!result_) result_ = identifierExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // concatExpr ((LT_OP | LT_KEYWORD | GT_OP | GT_KEYWORD
  //            | LE_OP | LE_KEYWORD | GE_OP | GE_KEYWORD
  //            | INSTANCEOF_KEYWORD) concatExpr)*
  public static boolean relExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "relExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, REL_EXPR, "<rel expr>");
    result_ = concatExpr(builder_, level_ + 1);
    result_ = result_ && relExpr_1(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  // ((LT_OP | LT_KEYWORD | GT_OP | GT_KEYWORD
  //            | LE_OP | LE_KEYWORD | GE_OP | GE_KEYWORD
  //            | INSTANCEOF_KEYWORD) concatExpr)*
  private static boolean relExpr_1(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "relExpr_1")) return false;
    while (true) {
      int pos_ = current_position_(builder_);
      if (!relExpr_1_0(builder_, level_ + 1)) break;
      if (!empty_element_parsed_guard_(builder_, "relExpr_1", pos_)) break;
    }
    return true;
  }

  // (LT_OP | LT_KEYWORD | GT_OP | GT_KEYWORD
  //            | LE_OP | LE_KEYWORD | GE_OP | GE_KEYWORD
  //            | INSTANCEOF_KEYWORD) concatExpr
  private static boolean relExpr_1_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "relExpr_1_0")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_);
    result_ = relExpr_1_0_0(builder_, level_ + 1);
    result_ = result_ && concatExpr(builder_, level_ + 1);
    exit_section_(builder_, marker_, null, result_);
    return result_;
  }

  // LT_OP | LT_KEYWORD | GT_OP | GT_KEYWORD
  //            | LE_OP | LE_KEYWORD | GE_OP | GE_KEYWORD
  //            | INSTANCEOF_KEYWORD
  private static boolean relExpr_1_0_0(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "relExpr_1_0_0")) return false;
    boolean result_;
    result_ = consumeToken(builder_, LT_OP);
    if (!result_) result_ = consumeToken(builder_, LT_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, GT_OP);
    if (!result_) result_ = consumeToken(builder_, GT_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, LE_OP);
    if (!result_) result_ = consumeToken(builder_, LE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, GE_OP);
    if (!result_) result_ = consumeToken(builder_, GE_KEYWORD);
    if (!result_) result_ = consumeToken(builder_, INSTANCEOF_KEYWORD);
    return result_;
  }

  /* ********************************************************** */
  // expression?
  static boolean root(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "root")) return false;
    expression(builder_, level_ + 1);
    return true;
  }

  /* ********************************************************** */
  // SINGLE_QUOTED_STRING | DOUBLE_QUOTED_STRING
  public static boolean stringLiteral(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "stringLiteral")) return false;
    if (!nextTokenIs(builder_, "<string literal>", DOUBLE_QUOTED_STRING, SINGLE_QUOTED_STRING)) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _NONE_, STRING_LITERAL, "<string literal>");
    result_ = consumeToken(builder_, SINGLE_QUOTED_STRING);
    if (!result_) result_ = consumeToken(builder_, DOUBLE_QUOTED_STRING);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // dotSuffix | bracketSuffix | callSuffix
  public static boolean suffix(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "suffix")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, SUFFIX, "<suffix>");
    result_ = dotSuffix(builder_, level_ + 1);
    if (!result_) result_ = bracketSuffix(builder_, level_ + 1);
    if (!result_) result_ = callSuffix(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

  /* ********************************************************** */
  // prefixExpr | accessExpr
  public static boolean unaryExpr(PsiBuilder builder_, int level_) {
    if (!recursion_guard_(builder_, level_, "unaryExpr")) return false;
    boolean result_;
    Marker marker_ = enter_section_(builder_, level_, _COLLAPSE_, UNARY_EXPR, "<unary expr>");
    result_ = prefixExpr(builder_, level_ + 1);
    if (!result_) result_ = accessExpr(builder_, level_ + 1);
    exit_section_(builder_, level_, marker_, result_, false, null);
    return result_;
  }

}
