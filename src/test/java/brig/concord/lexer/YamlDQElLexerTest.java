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
        // Raw: \"hello\" — YAML \" decoded to " → EL string "hello"
        assertTokenTypes("\\\"hello\\\"", ElTypes.DOUBLE_QUOTED_STRING);
    }

    @Test
    void yamlEscapedBackslashBeforeQuote() {
        // Raw: '\\'' — YAML \\ decoded to \, which EL-escapes the following '
        // Decoded: '\'' — EL string containing '
        assertTokenTypes("'\\\\''", ElTypes.SINGLE_QUOTED_STRING);
    }

    @Test
    void yamlEscapedBackslashElEscape() {
        // Raw: \"abc\\\"def\" — YAML \\ (→ \) + YAML \" (→ ") = EL escape \"
        // Decoded: "abc\"def" — EL string containing abc"def
        assertTokenTypes("\\\"abc\\\\\\\"def\\\"", ElTypes.DOUBLE_QUOTED_STRING);
    }

    @Test
    void methodCallWithYamlEscapedArgs() {
        // Raw: resource.replace('\"', '\\'')
        // Decoded: resource.replace('"', '\'')
        assertTokenTypes("resource.replace('\\\"', '\\\\'')",
                ElTypes.IDENTIFIER, ElTypes.DOT, ElTypes.IDENTIFIER,
                ElTypes.LPAREN,
                ElTypes.SINGLE_QUOTED_STRING, ElTypes.COMMA, TokenType.WHITE_SPACE,
                ElTypes.SINGLE_QUOTED_STRING,
                ElTypes.RPAREN);
    }

    @Test
    void otherYamlEscapesPassThrough() {
        // Raw: '\\n' — \n is a YAML escape, not \\ + n
        // Both chars pass through as-is: EL lexer sees \n inside string → \\.
        assertTokenTypes("'\\n'", ElTypes.SINGLE_QUOTED_STRING);
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
        String raw = "'\\\"'";
        lexer.start(raw, 0, raw.length(), 0);

        assertEquals(ElTypes.SINGLE_QUOTED_STRING, lexer.getTokenType());
        assertEquals(0, lexer.getTokenStart(), "token start (raw offset)");
        assertEquals(4, lexer.getTokenEnd(), "token end (raw offset)");
    }

    @Test
    void offsetMappingMultipleTokens() {
        // Raw: \\\"x\\\" (8 chars: \ \ " x \ \ " — wait, let me count)
        // Actually raw: \"x\" — 6 chars: \ " x \ "
        // Decoded: "x" — 3 chars: " x "
        var lexer = new YamlDQElLexer();
        String raw = "\\\"x\\\"";
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
        // Raw: a+\\\"b\\\" — a + \"b\"
        // Decoded: a+"b" — 5 tokens: IDENTIFIER, PLUS (actually += is CONCAT... let me use space)
        // Let me use: a \\\"b\\\"
        var lexer = new YamlDQElLexer();
        String raw = "a \\\"b\\\"";
        lexer.start(raw, 0, raw.length(), 0);

        int prevEnd = -1;
        int tokenCount = 0;
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