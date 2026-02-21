// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.el;

import brig.concord.el.psi.ElTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.junit5.impl.TestApplicationExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(TestApplicationExtension.class)
class ElLexerTest {

    @Test
    void simpleIdentifier() {
        assertTokenTypes("myVar", ElTypes.IDENTIFIER);
    }

    @Test
    void identifierWithHash() {
        assertTokenTypes("#implObj", ElTypes.IDENTIFIER);
    }

    @Test
    void integerLiteral() {
        assertTokenTypes("42", ElTypes.INTEGER_LITERAL);
    }

    @Test
    void floatLiteral() {
        assertTokenTypes("3.14", ElTypes.FLOAT_LITERAL);
    }

    @Test
    void floatWithExponent() {
        assertTokenTypes("1e5", ElTypes.FLOAT_LITERAL);
    }

    @Test
    void singleQuotedString() {
        assertTokenTypes("'hello'", ElTypes.SINGLE_QUOTED_STRING);
    }

    @Test
    void doubleQuotedString() {
        assertTokenTypes("\"hello\"", ElTypes.DOUBLE_QUOTED_STRING);
    }

    @Test
    void stringWithEscapes() {
        assertTokenTypes("'it\\'s'", ElTypes.SINGLE_QUOTED_STRING);
    }

    @Test
    void booleanKeywords() {
        assertTokenTypes("true", ElTypes.TRUE_KEYWORD);
        assertTokenTypes("false", ElTypes.FALSE_KEYWORD);
    }

    @Test
    void nullKeyword() {
        assertTokenTypes("null", ElTypes.NULL_KEYWORD);
    }

    @Test
    void keywordsNotIdentifiers() {
        assertTokenTypes("empty", ElTypes.EMPTY_KEYWORD);
        assertTokenTypes("not", ElTypes.NOT_KEYWORD);
        assertTokenTypes("and", ElTypes.AND_KEYWORD);
        assertTokenTypes("or", ElTypes.OR_KEYWORD);
        assertTokenTypes("div", ElTypes.DIV_KEYWORD);
        assertTokenTypes("mod", ElTypes.MOD_KEYWORD);
        assertTokenTypes("eq", ElTypes.EQ_KEYWORD);
        assertTokenTypes("ne", ElTypes.NE_KEYWORD);
        assertTokenTypes("lt", ElTypes.LT_KEYWORD);
        assertTokenTypes("gt", ElTypes.GT_KEYWORD);
        assertTokenTypes("le", ElTypes.LE_KEYWORD);
        assertTokenTypes("ge", ElTypes.GE_KEYWORD);
        assertTokenTypes("instanceof", ElTypes.INSTANCEOF_KEYWORD);
    }

    @Test
    void keywordPrefixIsIdentifier() {
        // "trueValue" should be IDENTIFIER, not TRUE_KEYWORD + IDENTIFIER
        assertTokenTypes("trueValue", ElTypes.IDENTIFIER);
        assertTokenTypes("nullable", ElTypes.IDENTIFIER);
        assertTokenTypes("empty_list", ElTypes.IDENTIFIER);
        assertTokenTypes("orDefault", ElTypes.IDENTIFIER);
    }

    @Test
    void multiCharOperators() {
        assertTokenTypes("+=", ElTypes.CONCAT);
        assertTokenTypes("->", ElTypes.ARROW);
        assertTokenTypes("==", ElTypes.EQ_OP);
        assertTokenTypes("!=", ElTypes.NE_OP);
        assertTokenTypes("<=", ElTypes.LE_OP);
        assertTokenTypes(">=", ElTypes.GE_OP);
        assertTokenTypes("&&", ElTypes.AND_OP);
        assertTokenTypes("||", ElTypes.OR_OP);
    }

    @Test
    void singleCharOperators() {
        assertTokenTypes("+", ElTypes.PLUS);
        assertTokenTypes("-", ElTypes.MINUS);
        assertTokenTypes("*", ElTypes.MULT);
        assertTokenTypes("/", ElTypes.DIV_OP);
        assertTokenTypes("%", ElTypes.MOD_OP);
        assertTokenTypes("!", ElTypes.NOT_OP);
        assertTokenTypes(".", ElTypes.DOT);
        assertTokenTypes(",", ElTypes.COMMA);
        assertTokenTypes(":", ElTypes.COLON);
        assertTokenTypes(";", ElTypes.SEMICOLON);
        assertTokenTypes("?", ElTypes.QUESTION);
        assertTokenTypes("=", ElTypes.ASSIGN);
    }

    @Test
    void brackets() {
        assertTokenTypes("(", ElTypes.LPAREN);
        assertTokenTypes(")", ElTypes.RPAREN);
        assertTokenTypes("[", ElTypes.LBRACKET);
        assertTokenTypes("]", ElTypes.RBRACKET);
        assertTokenTypes("{", ElTypes.LBRACE);
        assertTokenTypes("}", ElTypes.RBRACE);
    }

    @Test
    void propertyAccess() {
        assertTokenTypes("a.b", ElTypes.IDENTIFIER, ElTypes.DOT, ElTypes.IDENTIFIER);
    }

    @Test
    void methodCall() {
        assertTokenTypes("a.b()", ElTypes.IDENTIFIER, ElTypes.DOT, ElTypes.IDENTIFIER,
                ElTypes.LPAREN, ElTypes.RPAREN);
    }

    @Test
    void comparison() {
        assertTokenTypes("a > b",
                ElTypes.IDENTIFIER, TokenType.WHITE_SPACE,
                ElTypes.GT_OP, TokenType.WHITE_SPACE,
                ElTypes.IDENTIFIER);
    }

    @Test
    void complexExpression() {
        assertTokenTypes("a + b * 2",
                ElTypes.IDENTIFIER, TokenType.WHITE_SPACE,
                ElTypes.PLUS, TokenType.WHITE_SPACE,
                ElTypes.IDENTIFIER, TokenType.WHITE_SPACE,
                ElTypes.MULT, TokenType.WHITE_SPACE,
                ElTypes.INTEGER_LITERAL);
    }

    @Test
    void ternary() {
        assertTokenTypes("a ? b : c",
                ElTypes.IDENTIFIER, TokenType.WHITE_SPACE,
                ElTypes.QUESTION, TokenType.WHITE_SPACE,
                ElTypes.IDENTIFIER, TokenType.WHITE_SPACE,
                ElTypes.COLON, TokenType.WHITE_SPACE,
                ElTypes.IDENTIFIER);
    }

    @Test
    void lambda() {
        assertTokenTypes("x -> x + 1",
                ElTypes.IDENTIFIER, TokenType.WHITE_SPACE,
                ElTypes.ARROW, TokenType.WHITE_SPACE,
                ElTypes.IDENTIFIER, TokenType.WHITE_SPACE,
                ElTypes.PLUS, TokenType.WHITE_SPACE,
                ElTypes.INTEGER_LITERAL);
    }

    @Test
    void emptyOperator() {
        assertTokenTypes("empty list",
                ElTypes.EMPTY_KEYWORD, TokenType.WHITE_SPACE,
                ElTypes.IDENTIFIER);
    }

    @Test
    void functionCallWithArgs() {
        assertTokenTypes("hasVariable('x')",
                ElTypes.IDENTIFIER, ElTypes.LPAREN,
                ElTypes.SINGLE_QUOTED_STRING, ElTypes.RPAREN);
    }

    @Test
    void mapLiteral() {
        assertTokenTypes("{'a': 1}",
                ElTypes.LBRACE, ElTypes.SINGLE_QUOTED_STRING,
                ElTypes.COLON, TokenType.WHITE_SPACE,
                ElTypes.INTEGER_LITERAL, ElTypes.RBRACE);
    }

    @Test
    void listLiteral() {
        assertTokenTypes("[1, 2, 3]",
                ElTypes.LBRACKET, ElTypes.INTEGER_LITERAL,
                ElTypes.COMMA, TokenType.WHITE_SPACE,
                ElTypes.INTEGER_LITERAL, ElTypes.COMMA, TokenType.WHITE_SPACE,
                ElTypes.INTEGER_LITERAL, ElTypes.RBRACKET);
    }

    // --- helpers ---

    private static void assertTokenTypes(String input, IElementType... expectedTypes) {
        var actual = tokenize(input);
        assertThat(actual)
                .as("tokens for: %s", input)
                .containsExactly(expectedTypes);
    }

    private static List<IElementType> tokenize(String input) {
        var lexer = new ElLexer(null);
        lexer.reset(input, 0, input.length(), ElLexer.YYINITIAL);

        var tokens = new ArrayList<IElementType>();
        try {
            IElementType type;
            while ((type = lexer.advance()) != null) {
                tokens.add(type);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tokens;
    }
}
