// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

class ExprHighlightingTest extends HighlightingTestBase {

    @Test
    void testSimpleExpression() {
        configureFromText("""
            flows:
              main:
                - log: ${message}
            """);

        highlight(value("/flows/main[0]/log")).is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testExprInName() {
        configureFromText("""
            flows:
              main:
                - name: "Deploy '${release}' version"
                  task: http
            """);

        // DSL_LABEL comes from the annotator, EXPRESSION is on the ${...} delimiters via lexer
        highlight(value("/flows/main[0]/name"))
                .contains(ConcordHighlightingColors.DSL_LABEL);
        highlight(value("/flows/main[0]/name").substring("${release}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testExpressionWithDot() {
        configureFromText("""
            flows:
              main:
                - log: ${response.body}
            """);

        highlight(value("/flows/main[0]/log")).is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testExpressionInCondition() {
        configureFromText("""
            flows:
              main:
                - if: ${response.ok}
                  then:
                    - log: "success"
            """);

        highlight(value("/flows/main[0]/if")).is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testExpressionInTaskInput() {
        configureFromText("""
            flows:
              main:
                - task: "http"
                  in:
                    url: ${apiUrl}
                    headers:
                      Authorization: ${authToken}
            """);

        highlight(value("/flows/main[0]/in/url")).is(ConcordHighlightingColors.EXPRESSION);
        highlight(value("/flows/main[0]/in/headers/Authorization")).is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testComplexExpression() {
        configureFromText("""
            flows:
              main:
                - if: ${condition && (a > b || c == 'd')}
                  then:
                    - log: "complex"
            """);

        highlight(value("/flows/main[0]/if")).is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testQuotedScalarSingleExpression() {
        configureFromText("""
            flows:
              main:
                - log: "my-${expr}-value"
            """);

        highlight(value("/flows/main[0]/log").substring("${expr}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testQuotedScalarMultipleExpressions() {
        configureFromText("""
            flows:
              main:
                - log: "a-${x}-b-${y}-c"
            """);

        highlight(value("/flows/main[0]/log").substring("${x}"))
                .is(ConcordHighlightingColors.EXPRESSION);
        highlight(value("/flows/main[0]/log").substring("${y}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testQuotedScalarEscapedExpressionNotMatched() {
        configureFromText("""
            flows:
              main:
                - log: "a-\\${x}-b"
            """);

        highlight(value("/flows/main[0]/log").substring("\\${x}"))
                .isNot(ConcordHighlightingColors.EXPRESSION);

        highlight(value("/flows/main[0]/log"))
                .notContains(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testQuotedScalarNestedBracesInExpression() {
        configureFromText("""
            flows:
              main:
                - log: "a-${ foo({a: {b: 1}}) }-b"
            """);

        highlight(value("/flows/main[0]/log").substring("${ foo({a: {b: 1}}) }"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testBlockScalarFoldedSingleLineExpression() {
        configureFromText("""
            flows:
              main:
                - set:
                    slackMessageTitle: >-
                      result: ${hasVariable('snapshotVersion')}
            """);

        // Single-line expression in block scalar: delimiters highlighted via lexer
        highlight(value("/flows/main[0]/set/slackMessageTitle").substring("${hasVariable('snapshotVersion')}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testBlockScalarLiteralPipe() {
        configureFromText("""
            flows:
              main:
                - log: |
                    line1
                    has ${expr} here
                    and \\${escaped} here
                    also ${a{b}c} here
            """);

        highlight(value("/flows/main[0]/log").substring("${expr}"))
                .is(ConcordHighlightingColors.EXPRESSION);

        highlight(value("/flows/main[0]/log").substring("${a{b}c}"))
                .is(ConcordHighlightingColors.EXPRESSION);

        highlight(value("/flows/main[0]/log").substring("${escaped}"))
                .isNot(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testBlockScalarMultiLineExpression() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      text ${a +
                      b} more
            """);

        highlight(value("/flows/main[0]/set/result").substring("${"))
                .is(ConcordHighlightingColors.EXPRESSION);
        highlight(value("/flows/main[0]/set/result").substring("}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testBlockScalarMultiLineThreeLines() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      ${a +
                      b +
                      c}
            """);

        highlight(value("/flows/main[0]/set/result").substring("${"))
                .is(ConcordHighlightingColors.EXPRESSION);
        highlight(value("/flows/main[0]/set/result").substring("}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testBlockScalarMixedExpressions() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      ${single} and ${multi +
                      line}
            """);

        highlight(value("/flows/main[0]/set/result").substring("${single}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testFoldedScalarMultiLineExpression() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: >
                      text ${a +
                      b} more
            """);

        highlight(value("/flows/main[0]/set/result").substring("${"))
                .is(ConcordHighlightingColors.EXPRESSION);
        highlight(value("/flows/main[0]/set/result").substring("}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testBlockScalarNestedBracesMultiLine() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      ${foo({a:
                      1})}
            """);

        // ${ delimiter highlighted
        highlight(value("/flows/main[0]/set/result").substring("${"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testUnclosedExpressionInQuotedString() {
        configureFromText("""
            flows:
              main:
                - if: "${asdasdasdasdasdasd"
                  then:
                    - return
            """);

        // Unclosed ${ in a quoted string should NOT be treated as an expression
        highlight(value("/flows/main[0]/if"))
                .notContains(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testUnclosedExpressionWithValidExprBefore() {
        configureFromText("""
            flows:
              main:
                - log: "hello ${expr} ${unclosed"
            """);

        // The first complete expression should be highlighted
        highlight(value("/flows/main[0]/log").substring("${expr}"))
                .is(ConcordHighlightingColors.EXPRESSION);

        // The unclosed ${ should still produce expression highlighting (error is reported by parser)
        highlight(value("/flows/main[0]/log").substring("${unclosed"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    // --- Edge cases: broken/malformed expressions ---

    @Test
    void testEmptyExpression() {
        configureFromText("""
            flows:
              main:
                - log: ${}
            """);

        highlight(value("/flows/main[0]/log").substring("${"))
                .is(ConcordHighlightingColors.EXPRESSION);
        highlight(value("/flows/main[0]/log").substring("}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testEmptyExpressionInQuotedString() {
        configureFromText("""
            flows:
              main:
                - log: "before-${}-after"
            """);

        highlight(value("/flows/main[0]/log").substring("${"))
                .is(ConcordHighlightingColors.EXPRESSION);
        highlight(value("/flows/main[0]/log").substring("}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testJustDollarBraceAtEndOfQuotedString() {
        // "${" with no body and no closing brace
        configureFromText("""
            flows:
              main:
                - log: "hello ${"
                - log: done
            """);

        highlight(value("/flows/main[0]/log"))
                .notContains(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testMultipleUnclosedExpressionsInQuotedString() {
        configureFromText("""
            flows:
              main:
                - log: "${a ${b ${c"
            """);

        highlight(value("/flows/main[0]/log"))
                .notContains(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testAdjacentExpressions() {
        configureFromText("""
            flows:
              main:
                - log: "${a}${b}"
            """);

        highlight(value("/flows/main[0]/log").substring("${a}"))
                .is(ConcordHighlightingColors.EXPRESSION);
        highlight(value("/flows/main[0]/log").substring("${b}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testDoubleDollarBeforeExpression() {
        configureFromText("""
            flows:
              main:
                - log: "$${expr}"
            """);

        // $$ is not a valid expression start — the first $ is regular text,
        // then ${expr} is a valid expression
        highlight(value("/flows/main[0]/log").substring("${expr}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testUnclosedExpressionInPlainText() {
        // Plain text (TEXT) unclosed expression — continuation mode activates
        // but no continuation lines follow, so expression body is just the text
        configureFromText("""
            flows:
              main:
                - if: ${unclosed
                  then:
                    - log: done
            """);

        // ${ delimiter should still be highlighted even though expression is unclosed
        highlight(value("/flows/main[0]/if").substring("${"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testUnclosedExpressionInBlockScalar() {
        // Block scalar with expression that never closes
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      text ${unclosed
                      more text
                - log: done
            """);

        // ${ should still be highlighted
        highlight(value("/flows/main[0]/set/result").substring("${"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testEscapedThenRealExpression() {
        configureFromText("""
            flows:
              main:
                - log: "\\${escaped}${real}"
            """);

        // Escaped ${ should NOT be an expression
        highlight(value("/flows/main[0]/log").substring("\\${escaped}"))
                .isNot(ConcordHighlightingColors.EXPRESSION);

        // Real ${ should be highlighted
        highlight(value("/flows/main[0]/log").substring("${real}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testExpressionInSingleQuotedYamlString() {
        configureFromText("""
            flows:
              main:
                - log: '${expr}'
            """);

        // Even in YAML single-quoted strings, Concord resolves ${...} expressions
        highlight(value("/flows/main[0]/log").substring("${expr}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testUnclosedExpressionInSingleQuotedYamlString() {
        configureFromText("""
            flows:
              main:
                - log: '${unclosed'
                - log: done
            """);

        highlight(value("/flows/main[0]/log"))
                .notContains(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testExpressionFollowedByUnclosedOnNextStep() {
        // Verify unclosed expr doesn't bleed into subsequent steps
        configureFromText("""
            flows:
              main:
                - log: "${unclosed"
                - log: ${valid}
            """);

        // First value: unclosed, no expression highlighting
        highlight(value("/flows/main[0]/log"))
                .notContains(ConcordHighlightingColors.EXPRESSION);

        // Second value: valid expression, should still work
        highlight(value("/flows/main[1]/log")).is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    void testBlockScalarMultiLineWithQuotes() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      ${a + 'hello
                      world'}
            """);

        highlight(value("/flows/main[0]/set/result").substring("${"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }
}
