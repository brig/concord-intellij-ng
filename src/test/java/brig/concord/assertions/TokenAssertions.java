package brig.concord.assertions;

import brig.concord.lexer.ConcordYAMLFlexLexer;
import brig.concord.lexer.ExpressionSplittingLexer;
import com.intellij.lexer.Lexer;
import com.intellij.psi.tree.IElementType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TokenAssertions {

    public static TokenAssertions assertTokens(String yaml) {
        return assertTokens(yaml, 200);
    }

    public static TokenAssertions assertTokens(String yaml, int maxTokens) {
        return new TokenAssertions(yaml, new ConcordYAMLFlexLexer(), maxTokens);
    }

    public static TokenAssertions assertTokensWithExprSplitting(String yaml) {
        return assertTokensWithExprSplitting(yaml, 200);
    }

    public static TokenAssertions assertTokensWithExprSplitting(String yaml, int maxTokens) {
        return new TokenAssertions(yaml, new ExpressionSplittingLexer(new ConcordYAMLFlexLexer()), maxTokens);
    }

    public record TokenInfo(IElementType type, int state, int start, int end, String text) {
        public String typeName() {
            return type.toString();
        }

        @Override
        public String toString() {
            var escapedText = text.replace("\n", "\\n").replace("\t", "\\t");
            return String.format("%-25s state=%-3d [%3d-%3d]: '%s'", type, state, start, end, escapedText);
        }
    }

    private final List<TokenInfo> tokens;
    private final String yaml;

    private TokenAssertions(String yaml, Lexer lexer, int maxTokens) {
        this.yaml = yaml;
        this.tokens = tokenize(yaml, lexer, maxTokens);
    }

    public TokenAssertions hasCount(String tokenName, int expected) {
        long actual = countTokens(tokenName);
        assertEquals(expected, actual,
                () -> "Expected " + expected + " " + tokenName + " tokens, found " + actual + ".\n" + formatTokens());
        return this;
    }

    public TokenAssertions hasToken(String tokenName) {
        long count = countTokens(tokenName);
        assertTrue(count > 0,
                () -> "Expected at least one " + tokenName + " token.\n" + formatTokens());
        return this;
    }

    public TokenAssertions noToken(String tokenName) {
        long count = countTokens(tokenName);
        assertEquals(0, count,
                () -> "Expected no " + tokenName + " tokens, found " + count + ".\n" + formatTokens());
        return this;
    }

    public SingleTokenAssert token(String tokenName) {
        return token(tokenName, 0);
    }

    public SingleTokenAssert token(String tokenName, int index) {
        var matching = tokens.stream()
                .filter(t -> t.typeName().equals(tokenName))
                .toList();
        assertFalse(matching.isEmpty(),
                () -> "No " + tokenName + " tokens found.\n" + formatTokens());
        assertTrue(index < matching.size(),
                () -> "Only " + matching.size() + " " + tokenName + " tokens found, requested index " + index + ".\n" + formatTokens());
        return new SingleTokenAssert(this, matching.get(index));
    }

    public TokenAssertions tokenHasText(String tokenName, String expectedText) {
        return token(tokenName).hasText(expectedText).and();
    }

    public TokenAssertions debugPrint() {
        System.out.println(formatTokens());
        return this;
    }

    public String formatTokens() {
        var sb = new StringBuilder();
        tokens.forEach(t -> sb.append(t).append("\n"));
        return sb.toString();
    }

    public List<TokenInfo> getTokens() {
        return tokens;
    }

    private long countTokens(String tokenName) {
        return tokens.stream()
                .filter(t -> t.typeName().equals(tokenName))
                .count();
    }

    private static List<TokenInfo> tokenize(String yaml, Lexer lexer, int maxTokens) {
        lexer.start(yaml, 0, yaml.length(), 0);

        var tokens = new ArrayList<TokenInfo>();
        IElementType token;

        while ((token = lexer.getTokenType()) != null && tokens.size() < maxTokens) {
            tokens.add(new TokenInfo(
                    token,
                    lexer.getState(),
                    lexer.getTokenStart(),
                    lexer.getTokenEnd(),
                    yaml.substring(lexer.getTokenStart(), lexer.getTokenEnd())
            ));
            lexer.advance();
        }

        return tokens;
    }

    public static class SingleTokenAssert {
        private final TokenAssertions parent;
        private final TokenInfo token;

        SingleTokenAssert(TokenAssertions parent, TokenInfo token) {
            this.parent = parent;
            this.token = token;
        }

        public SingleTokenAssert hasText(String expected) {
            assertEquals(expected, token.text(),
                    () -> "Token " + token.typeName() + " expected text '" + expected + "', got '" + token.text() + "'.\n" + parent.formatTokens());
            return this;
        }

        public SingleTokenAssert textContains(String substring) {
            assertTrue(token.text().contains(substring),
                    () -> "Token " + token.typeName() + " expected to contain '" + substring + "', got '" + token.text() + "'.\n" + parent.formatTokens());
            return this;
        }

        public SingleTokenAssert followedBy(String tokenName) {
            int index = parent.tokens.indexOf(token);
            assertTrue(index + 1 < parent.tokens.size(),
                    () -> "Token " + token.typeName() + " is the last token, cannot check followedBy.\n" + parent.formatTokens());
            var next = parent.tokens.get(index + 1);
            assertEquals(tokenName, next.typeName(),
                    () -> "Expected " + token.typeName() + " to be followed by " + tokenName + ", got " + next.typeName() + ".\n" + parent.formatTokens());
            return this;
        }

        public TokenAssertions and() {
            return parent;
        }
    }
}