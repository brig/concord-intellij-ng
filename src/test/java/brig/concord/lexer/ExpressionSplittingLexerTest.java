// SPDX-License-Identifier: Apache-2.0
package brig.concord.lexer;

import brig.concord.assertions.TokenAssertions;
import com.intellij.lexer.RestartableLexer;
import org.junit.jupiter.api.Test;

import static brig.concord.assertions.TokenAssertions.assertTokensWithExprSplitting;
import static org.junit.jupiter.api.Assertions.*;

class ExpressionSplittingLexerTest {

    @Test
    void plainTextNoExpressions() {
        assertTokensWithExprSplitting("- log: hello")
                .noToken("el expr start")
                .noToken("el expr body")
                .noToken("el expr end")
                .hasToken("text");
    }

    @Test
    void simpleExpressionInPlainText() {
        assertTokensWithExprSplitting("- log: ${expr}")
                .hasCount("el expr start", 1)
                .hasCount("el expr body", 1)
                .hasCount("el expr end", 1)
                .token("el expr start").hasText("${").and()
                .token("el expr body").hasText("expr").and()
                .token("el expr end").hasText("}");
    }

    @Test
    void expressionInDoubleQuotedString() {
        assertTokensWithExprSplitting("- log: \"pre ${x} post\"")
                .hasCount("el expr start", 1)
                .hasCount("el expr body", 1)
                .hasCount("el expr end", 1)
                .token("scalar dstring", 0).hasText("\"pre ").and()
                .token("el expr start").hasText("${").and()
                .token("el expr body").hasText("x").and()
                .token("el expr end").hasText("}").and()
                .token("scalar dstring", 1).hasText(" post\"");
    }

    @Test
    void expressionInSingleQuotedString() {
        assertTokensWithExprSplitting("- log: 'a ${b} c'")
                .hasCount("el expr start", 1)
                .hasCount("el expr body", 1)
                .hasCount("el expr end", 1)
                .token("scalar string", 0).hasText("'a ").and()
                .token("el expr start").hasText("${").and()
                .token("el expr body").hasText("b").and()
                .token("el expr end").hasText("}").and()
                .token("scalar string", 1).hasText(" c'");
    }

    @Test
    void multipleExpressions() {
        assertTokensWithExprSplitting("- log: \"${a} ${b}\"")
                .hasCount("el expr start", 2)
                .hasCount("el expr body", 2)
                .hasCount("el expr end", 2)
                .token("el expr body", 0).hasText("a").and()
                .token("el expr body", 1).hasText("b").and()
                .token("scalar dstring", 0).hasText("\"").and()
                .token("scalar dstring", 1).hasText(" ").and()
                .token("scalar dstring", 2).hasText("\"");
    }

    @Test
    void escapedDollarBrace() {
        assertTokensWithExprSplitting("- log: \"a \\${x} b\"")
                .noToken("el expr start")
                .noToken("el expr body")
                .noToken("el expr end");
    }

    @Test
    void emptyExpression() {
        assertTokensWithExprSplitting("- log: ${}")
                .hasCount("el expr start", 1)
                .hasCount("el expr end", 1)
                .noToken("el expr body")
                .token("el expr start").hasText("${").and()
                .token("el expr end").hasText("}");
    }

    @Test
    void nestedBraces() {
        assertTokensWithExprSplitting("- log: \"${foo({a: 1})}\"")
                .hasCount("el expr start", 1)
                .hasCount("el expr body", 1)
                .hasCount("el expr end", 1)
                .token("el expr body").hasText("foo({a: 1})");
    }

    @Test
    void unclosedExprInQuotedString() {
        assertTokensWithExprSplitting("- log: \"${unclosed\"")
                .hasCount("el expr start", 1)
                .hasCount("el expr body", 1)
                .noToken("el expr end")
                .token("el expr start").hasText("${").and()
                .token("el expr body").hasText("unclosed\"");
    }

    @Test
    void unclosedExprInPlainText() {
        assertTokensWithExprSplitting("- log: ${unclosed")
                .hasCount("el expr start", 1)
                .hasCount("el expr body", 1)
                .token("el expr start").hasText("${").and()
                .token("el expr body").hasText("unclosed");
    }

    @Test
    void multiLineInBlockScalar() {
        assertTokensWithExprSplitting("result: |\n  text ${a +\n  b} more")
                .hasCount("el expr start", 1)
                .hasCount("el expr end", 1)
                .token("el expr start").hasText("${").and()
                .token("el expr body", 0).hasText("a +").and()
                .token("el expr end").hasText("}");
    }

    @Test
    void adjacentExpressions() {
        assertTokensWithExprSplitting("- log: \"${a}${b}\"")
                .hasCount("el expr start", 2)
                .hasCount("el expr body", 2)
                .hasCount("el expr end", 2)
                .token("el expr body", 0).hasText("a").and()
                .token("el expr body", 1).hasText("b");
    }

    @Test
    void escapedThenReal() {
        assertTokensWithExprSplitting("- log: \"\\${a}${b}\"")
                .hasCount("el expr start", 1)
                .hasCount("el expr body", 1)
                .hasCount("el expr end", 1)
                .token("el expr body").hasText("b");
    }

    @Test
    void escapedQuoteInsideExpressionString() {
        // ${"abc\"}"} — the \" is an escaped quote, so the } inside the string is not a closing brace
        assertTokensWithExprSplitting("- log: ${\"abc\\\"}\"}") // yaml: - log: ${"abc\"}"}
                .hasCount("el expr start", 1)
                .hasCount("el expr body", 1)
                .hasCount("el expr end", 1)
                .token("el expr body").hasText("\"abc\\\"}\"");
    }

    @Test
    void escapedSingleQuoteInsideExpressionString() {
        // ${'abc\'}'} — escaped single quote
        assertTokensWithExprSplitting("- log: ${'abc\\'}'}")
                .hasCount("el expr start", 1)
                .hasCount("el expr body", 1)
                .hasCount("el expr end", 1)
                .token("el expr body").hasText("'abc\\'}'");
    }

    @Test
    void quotesInsideExpression() {
        assertTokensWithExprSplitting("- log: ${foo('bar')}")
                .hasCount("el expr start", 1)
                .hasCount("el expr body", 1)
                .hasCount("el expr end", 1)
                .token("el expr body").hasText("foo('bar')");
    }

    @Test
    void validExprBeforeUnclosedInQuotedString() {
        assertTokensWithExprSplitting("- log: \"${ok} ${bad\"")
                .hasCount("el expr start", 2)
                .hasCount("el expr body", 2)
                .hasCount("el expr end", 1)
                .token("el expr body", 0).hasText("ok").and()
                .token("el expr body", 1).hasText("bad\"");
    }

    @Test
    void multiLineBlockScalarTokensAreContiguous() {
        // Reproducer for InvalidStateException: discontinuous tokens during incremental re-lexing.
        // The multi-line expression spans 3 lines in a block scalar, which exercises
        // continuation mode and splitContinuation with closing } on a different line.
        String yaml = """
                flows:
                  main:
                    - log: "Hello, ${userName}!"
                    - if: ${status == "active"}
                      then:
                        - set:
                            message: |
                              ${hasVariable('env') ?
                              "Production" :
                              "Development"}
                        - log: \\${escaped} is plain text""";

        var result = assertTokensWithExprSplitting(yaml);
        var tokens = result.getTokens();

        // Verify contiguity: each token must start where the previous one ended
        for (int i = 1; i < tokens.size(); i++) {
            var prev = tokens.get(i - 1);
            var curr = tokens.get(i);
            int idx = i;
            assertEquals(prev.end(), curr.start(),
                    () -> "Gap between tokens " + (idx - 1) + " and " + idx + ":\n"
                            + "  prev: " + prev + "\n"
                            + "  curr: " + curr + "\n"
                            + result.formatTokens());
        }
    }

    @Test
    void restartAtAnyRestartableStateProducesContiguousTokens() {
        // Verifies the RestartableLexer contract: restarting at any position whose state
        // is marked restartable must produce contiguous tokens from that point onward.
        String yaml = """
                flows:
                  main:
                    - set:
                        message: |
                          ${hasVariable('env') ?
                          "Production" :
                          "Development"}
                    - log: done""";

        var result = assertTokensWithExprSplitting(yaml);
        var tokens = result.getTokens();

        var lexer = new ExpressionSplittingLexer(new ConcordYAMLFlexLexer());

        for (int i = 0; i < tokens.size(); i++) {
            var token = tokens.get(i);
            if (!lexer.isRestartableState(token.state())) {
                continue;
            }

            // Restart at this restartable position
            lexer.start(yaml, token.start(), yaml.length(), token.state());

            // Verify the first token produced matches the original
            assertNotNull(lexer.getTokenType(),
                    () -> "Restart at offset " + token.start() + " (state " + token.state()
                            + ") produced null token.\nTokens:\n" + result.formatTokens());
            assertEquals(token.start(), lexer.getTokenStart(),
                    () -> "Restart at offset " + token.start() + " (state " + token.state()
                            + ") produced token starting at " + lexer.getTokenStart()
                            + ".\nTokens:\n" + result.formatTokens());

            // Verify at least a few tokens are contiguous after restart
            int prevEnd = lexer.getTokenEnd();
            var prevType = lexer.getTokenType();
            lexer.advance();
            for (int j = 0; j < 5 && lexer.getTokenType() != null; j++) {
                int curStart = lexer.getTokenStart();
                int pe = prevEnd;
                var pt = prevType;
                assertEquals(pe, curStart,
                        () -> "Discontinuous token after restart at offset " + token.start()
                                + " (state " + token.state() + "): prev " + pt + " ended at " + pe
                                + ", next " + lexer.getTokenType() + " starts at " + curStart
                                + ".\nTokens:\n" + result.formatTokens());
                prevEnd = lexer.getTokenEnd();
                prevType = lexer.getTokenType();
                lexer.advance();
            }
        }
    }
}
