package brig.concord.el;

import com.intellij.psi.tree.IElementType;
import com.intellij.lexer.FlexLexer;

import static brig.concord.el.psi.ElTypes.*;
import static com.intellij.psi.TokenType.*;

%%

%class ElLexer
%implements FlexLexer
%unicode
%public
%function advance
%type IElementType

WHITE_SPACE      = [ \t\n\r]+
INTEGER          = [0-9]+
FLOAT            = [0-9]*\.[0-9]+([eE][+-]?[0-9]+)?|[0-9]+[eE][+-]?[0-9]+
SINGLE_QUOTED    = '([^'\\]|\\.)*'
DOUBLE_QUOTED    = \"([^\"\\]|\\.)*\"
// In YAML double-quoted strings, the EL body text contains YAML escapes:
// literal " appears as \" in the raw text. This pattern matches EL strings
// whose delimiters are YAML-escaped: \"...\"
YAML_DQ_DOUBLE_QUOTED = \\\"([^\"\\]|\\.)*\\\"

// EL identifiers: letters, $, _, # (for implicit objects like #sessionScope)
// Unicode letter ranges from EL 3.0 spec (ELParser.jjt lines 559-574)
LETTER           = [\u0024\u0041-\u005a\u005f\u0061-\u007a\u00c0-\u00d6\u00d8-\u00f6\u00f8-\u00ff\u0100-\u1fff\u3040-\u318f\u3300-\u337f\u3400-\u3d2d\u4e00-\u9fff\uf900-\ufaff]
DIGIT            = [\u0030-\u0039\u0660-\u0669\u06f0-\u06f9\u0966-\u096f\u09e6-\u09ef\u0a66-\u0a6f\u0ae6-\u0aef\u0b66-\u0b6f\u0be7-\u0bef\u0c66-\u0c6f\u0ce6-\u0cef\u0d66-\u0d6f\u0e50-\u0e59\u0ed0-\u0ed9\u1040-\u1049]
IMPL_OBJ_START   = "#"
IDENTIFIER       = ({LETTER}|{IMPL_OBJ_START})({LETTER}|{DIGIT})*

%%

{WHITE_SPACE}    { return WHITE_SPACE; }

// Multi-char operators (must match before single-char)
"+="             { return CONCAT; }
"->"             { return ARROW; }
"=="             { return EQ_OP; }
"!="             { return NE_OP; }
"<="             { return LE_OP; }
">="             { return GE_OP; }
"&&"             { return AND_OP; }
"||"             { return OR_OP; }

// Single-char operators
"+"              { return PLUS; }
"-"              { return MINUS; }
"*"              { return MULT; }
"/"              { return DIV_OP; }
"%"              { return MOD_OP; }
"="              { return ASSIGN; }
"<"              { return LT_OP; }
">"              { return GT_OP; }
"!"              { return NOT_OP; }
"."              { return DOT; }
","              { return COMMA; }
":"              { return COLON; }
";"              { return SEMICOLON; }
"?"              { return QUESTION; }
"("              { return LPAREN; }
")"              { return RPAREN; }
"["              { return LBRACKET; }
"]"              { return RBRACKET; }
"{"              { return LBRACE; }
"}"              { return RBRACE; }

// Strings — YAML-escaped variant handles \" delimiters in YAML DQ contexts
{YAML_DQ_DOUBLE_QUOTED} { return DOUBLE_QUOTED_STRING; }
{SINGLE_QUOTED}  { return SINGLE_QUOTED_STRING; }
{DOUBLE_QUOTED}  { return DOUBLE_QUOTED_STRING; }

// Numbers (float before integer — longest match wins)
{FLOAT}          { return FLOAT_LITERAL; }
{INTEGER}        { return INTEGER_LITERAL; }

// Keywords (before IDENTIFIER — same length, first rule wins in JFlex)
"true"           { return TRUE_KEYWORD; }
"false"          { return FALSE_KEYWORD; }
"null"           { return NULL_KEYWORD; }
"empty"          { return EMPTY_KEYWORD; }
"not"            { return NOT_KEYWORD; }
"and"            { return AND_KEYWORD; }
"or"             { return OR_KEYWORD; }
"div"            { return DIV_KEYWORD; }
"mod"            { return MOD_KEYWORD; }
"eq"             { return EQ_KEYWORD; }
"ne"             { return NE_KEYWORD; }
"lt"             { return LT_KEYWORD; }
"gt"             { return GT_KEYWORD; }
"le"             { return LE_KEYWORD; }
"ge"             { return GE_KEYWORD; }
"instanceof"     { return INSTANCEOF_KEYWORD; }

// Identifiers
{IDENTIFIER}     { return IDENTIFIER; }

// Bad character fallback
[^]              { return BAD_CHARACTER; }
