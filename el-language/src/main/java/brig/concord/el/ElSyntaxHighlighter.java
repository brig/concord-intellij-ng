// SPDX-License-Identifier: Apache-2.0
package brig.concord.el;

import brig.concord.el.psi.ElTypes;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class ElSyntaxHighlighter extends SyntaxHighlighterBase {

    private static final TextAttributesKey[] EMPTY = TextAttributesKey.EMPTY_ARRAY;

    private static final Map<IElementType, TextAttributesKey[]> TOKEN_MAP = buildTokenMap();

    private static Map<IElementType, TextAttributesKey[]> buildTokenMap() {
        Map<IElementType, TextAttributesKey[]> m = new HashMap<>();

        // keywords
        TextAttributesKey[] keyword = {ElHighlightingColors.KEYWORD};
        for (IElementType type : ElTokenSets.KEYWORDS.getTypes()) {
            m.put(type, keyword);
        }

        // strings
        TextAttributesKey[] string = {ElHighlightingColors.STRING};
        m.put(ElTypes.SINGLE_QUOTED_STRING, string);
        m.put(ElTypes.DOUBLE_QUOTED_STRING, string);

        // numbers
        TextAttributesKey[] number = {ElHighlightingColors.NUMBER};
        m.put(ElTypes.INTEGER_LITERAL, number);
        m.put(ElTypes.FLOAT_LITERAL, number);

        // identifier
        m.put(ElTypes.IDENTIFIER, new TextAttributesKey[]{ElHighlightingColors.IDENTIFIER});

        // operators
        TextAttributesKey[] operator = {ElHighlightingColors.OPERATION_SIGN};
        m.put(ElTypes.PLUS, operator);
        m.put(ElTypes.MINUS, operator);
        m.put(ElTypes.MULT, operator);
        m.put(ElTypes.DIV_OP, operator);
        m.put(ElTypes.MOD_OP, operator);
        m.put(ElTypes.EQ_OP, operator);
        m.put(ElTypes.NE_OP, operator);
        m.put(ElTypes.LT_OP, operator);
        m.put(ElTypes.GT_OP, operator);
        m.put(ElTypes.LE_OP, operator);
        m.put(ElTypes.GE_OP, operator);
        m.put(ElTypes.AND_OP, operator);
        m.put(ElTypes.OR_OP, operator);
        m.put(ElTypes.NOT_OP, operator);
        m.put(ElTypes.ASSIGN, operator);
        m.put(ElTypes.CONCAT, operator);
        m.put(ElTypes.ARROW, operator);
        m.put(ElTypes.QUESTION, operator);
        m.put(ElTypes.COLON, operator);

        // parentheses
        TextAttributesKey[] parentheses = {ElHighlightingColors.PARENTHESES};
        m.put(ElTypes.LPAREN, parentheses);
        m.put(ElTypes.RPAREN, parentheses);

        // brackets
        TextAttributesKey[] brackets = {ElHighlightingColors.BRACKETS};
        m.put(ElTypes.LBRACKET, brackets);
        m.put(ElTypes.RBRACKET, brackets);

        // braces
        TextAttributesKey[] braces = {ElHighlightingColors.BRACES};
        m.put(ElTypes.LBRACE, braces);
        m.put(ElTypes.RBRACE, braces);

        // dot
        m.put(ElTypes.DOT, new TextAttributesKey[]{ElHighlightingColors.DOT});

        // comma
        m.put(ElTypes.COMMA, new TextAttributesKey[]{ElHighlightingColors.COMMA});

        // semicolon
        m.put(ElTypes.SEMICOLON, new TextAttributesKey[]{ElHighlightingColors.SEMICOLON});

        // bad character
        m.put(TokenType.BAD_CHARACTER, new TextAttributesKey[]{ElHighlightingColors.BAD_CHARACTER});

        return Map.copyOf(m);
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new ElLexerAdapter();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return TOKEN_MAP.getOrDefault(tokenType, EMPTY);
    }
}
