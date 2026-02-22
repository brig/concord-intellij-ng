// SPDX-License-Identifier: Apache-2.0
package brig.concord.lexer;

import brig.concord.el.psi.ElTypes;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.junit5.impl.TestApplicationExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link YamlDQElLexer} — verifies that YAML double-quoted escape sequences
 * are decoded before EL lexing and that raw offset mapping is correct.
 */
@ExtendWith(TestApplicationExtension.class)
class YamlDQElLexerTest {

    @Test
    void noEscapesPassthrough() {
        // No YAML escapes — identical to plain EL lexer
        assertTokenTypes("a + b",
                ElTypes.IDENTIFIER, TokenType.WHITE_SPACE,
                ElTypes.PLUS, TokenType.WHITE_SPACE,
                ElTypes.IDENTIFIER);
    }

    @Test
    void yamlEscapedDoubleQuoteBecomesStringDelimiter() {
        assertTokenTypes("\\\"hello\\\"", ElTypes.DOUBLE_QUOTED_STRING);
    }

    @Test
    void yamlEscapedBackslashBeforeQuote() {
        assertTokenTypes("'\\\\''", ElTypes.SINGLE_QUOTED_STRING);
    }

    @Test
    void yamlEscapedBackslashElEscape() {
        assertTokenTypes("\\\"abc\\\\\\\"def\\\"", ElTypes.DOUBLE_QUOTED_STRING);
    }

    @Test
    void methodCallWithYamlEscapedArgs() {
        assertTokenTypes("resource.replace('\\\"', '\\\\'')",
                ElTypes.IDENTIFIER, ElTypes.DOT, ElTypes.IDENTIFIER,
                ElTypes.LPAREN,
                ElTypes.SINGLE_QUOTED_STRING, ElTypes.COMMA, TokenType.WHITE_SPACE,
                ElTypes.SINGLE_QUOTED_STRING,
                ElTypes.RPAREN);
    }

    @Test
    void otherYamlEscapesPassThrough() {
        assertTokenTypes("'\\n'", ElTypes.SINGLE_QUOTED_STRING);
    }

    @Test
    void yamlLineFoldingDecodedToNothing() {
        // Raw: secrets.waitFor('secretName',\<newline>      \ 20)
        // \<newline> is YAML line folding (removes newline + trims leading whitespace)
        // \ <space> is YAML escaped space
        // Decoded: secrets.waitFor('secretName', 20)
        assertTokenTypes("secrets.waitFor('secretName',\\\n      \\ 20)",
                ElTypes.IDENTIFIER, ElTypes.DOT, ElTypes.IDENTIFIER,
                ElTypes.LPAREN,
                ElTypes.SINGLE_QUOTED_STRING, ElTypes.COMMA,
                TokenType.WHITE_SPACE,
                ElTypes.INTEGER_LITERAL,
                ElTypes.RPAREN);
    }

    @Test
    void yamlLineFoldingOffsets() {
        // Verify offset mapping for line folding
        var lexer = new YamlDQElLexer();
        // Raw: a , \ \n <4 spaces> \ <space> b  (11 chars)
        // Decoded: a , <space> b  (4 chars)
        // The \<newline><spaces> folds to nothing; the "\ " decodes to space
        var raw = "a,\\\n    \\ b";
        lexer.start(raw, 0, raw.length(), 0);

        assertEquals(ElTypes.IDENTIFIER, lexer.getTokenType());
        assertEquals(0, lexer.getTokenStart());
        assertEquals(1, lexer.getTokenEnd());
        lexer.advance();

        // COMMA ends at raw 8: the folded chars (positions 2-7) are absorbed
        assertEquals(ElTypes.COMMA, lexer.getTokenType());
        assertEquals(1, lexer.getTokenStart());
        assertEquals(8, lexer.getTokenEnd());
        lexer.advance();

        // Space decoded from "\ " at raw positions [8, 10)
        assertEquals(TokenType.WHITE_SPACE, lexer.getTokenType());
        assertEquals(8, lexer.getTokenStart());
        assertEquals(10, lexer.getTokenEnd());
        lexer.advance();

        assertEquals(ElTypes.IDENTIFIER, lexer.getTokenType());
        assertEquals(10, lexer.getTokenStart());
        assertEquals(11, lexer.getTokenEnd());
    }

    @Test
    void loneBackslashAtEnd() {
        // Trailing \ without following char — passes through as-is
        assertTokenTypes("a\\",
                ElTypes.IDENTIFIER, TokenType.BAD_CHARACTER);
    }

    @Test
    void offsetMappingCorrectness() {
        // Raw: '\"' (4 chars: ' \ " ')
        // Decoded: '"' (3 chars: ' " ')
        // Token should span raw [0, 4)
        var lexer = new YamlDQElLexer();
        var raw = "'\\\"'";
        lexer.start(raw, 0, raw.length(), 0);

        assertEquals(ElTypes.SINGLE_QUOTED_STRING, lexer.getTokenType());
        assertEquals(0, lexer.getTokenStart(), "token start (raw offset)");
        assertEquals(4, lexer.getTokenEnd(), "token end (raw offset)");
    }

    @Test
    void offsetMappingMultipleTokens() {
        var lexer = new YamlDQElLexer();
        var raw = "\\\"x\\\"";
        lexer.start(raw, 0, raw.length(), 0);

        // Single token: DOUBLE_QUOTED_STRING
        assertEquals(ElTypes.DOUBLE_QUOTED_STRING, lexer.getTokenType());
        assertEquals(0, lexer.getTokenStart());
        assertEquals(5, lexer.getTokenEnd());

        // Verify buffer sequence returns raw text
        assertEquals(raw, lexer.getBufferSequence().toString());
    }

    @Test
    void contiguousTokensWithEscapes() {
        var lexer = new YamlDQElLexer();
        var raw = "a \\\"b\\\"";
        lexer.start(raw, 0, raw.length(), 0);

        var prevEnd = -1;
        var tokenCount = 0;
        while (lexer.getTokenType() != null) {
            if (prevEnd >= 0) {
                assertEquals(prevEnd, lexer.getTokenStart(),
                        "Gap between tokens at index " + tokenCount);
            }
            prevEnd = lexer.getTokenEnd();
            tokenCount++;
            lexer.advance();
        }
        // a, SPACE, "b" — 3 tokens
        assertEquals(3, tokenCount);
        assertEquals(raw.length(), prevEnd, "Last token should end at buffer end");
    }

    // --- helpers ---

    private static void assertTokenTypes(String rawInput, IElementType... expectedTypes) {
        var actual = tokenize(rawInput);
        assertThat(actual)
                .as("tokens for raw input: %s", rawInput)
                .containsExactly(expectedTypes);
    }

    private static List<IElementType> tokenize(String rawInput) {
        var lexer = new YamlDQElLexer();
        lexer.start(rawInput, 0, rawInput.length(), 0);

        var tokens = new ArrayList<IElementType>();
        while (lexer.getTokenType() != null) {
            tokens.add(lexer.getTokenType());
            lexer.advance();
        }
        return tokens;
    }
}
