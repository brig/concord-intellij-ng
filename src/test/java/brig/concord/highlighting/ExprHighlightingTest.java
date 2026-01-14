package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

public class ExprHighlightingTest extends HighlightingTestBase {

    @Test
    public void testSimpleExpression() {
        configureFromText("""
            flows:
              main:
                - log: ${message}
            """);

        highlight(value("/flows/main[0]/log")).is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    public void testExprInName() {
        configureFromText("""
            flows:
              main:
                - name: "Deploy '${release}' version"
                  task: http
            """);

        highlight(value("/flows/main[0]/name"))
                .containsAll(ConcordHighlightingColors.EXPRESSION, ConcordHighlightingColors.DSL_LABEL);
    }

    @Test
    public void testExpressionWithDot() {
        configureFromText("""
            flows:
              main:
                - log: ${response.body}
            """);

        highlight(value("/flows/main[0]/log")).is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    public void testExpressionInCondition() {
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
    public void testExpressionInTaskInput() {
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
    public void testComplexExpression() {
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
    public void testQuotedScalarSingleExpression() {
        configureFromText("""
            flows:
              main:
                - log: "my-${expr}-value"
            """);

        highlight(value("/flows/main[0]/log").substring("${expr}"))
                .is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    public void testQuotedScalarMultipleExpressions() {
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
    public void testQuotedScalarEscapedExpressionNotMatched() {
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
    public void testQuotedScalarNestedBracesInExpression() {
        configureFromText("""
            flows:
              main:
                - log: "a-${ foo({a: {b: 1}}) }-b"
            """);

        highlight(value("/flows/main[0]/log").substring("${ foo({a: {b: 1}}) }"))
                .is(ConcordHighlightingColors.EXPRESSION);

        highlight(value("/flows/main[0]/log"))
                .containsExactly(ConcordHighlightingColors.EXPRESSION, 1);
    }

    @Test
    public void testBlockScalarFoldedGreaterThan() {
        configureFromText("""
            flows:
              main:
                - set:
                    slackMessageTitle: >-
                      ${hasVariable('snapshotVersion') ?
                        "*Deployment was successful*" :
                        "*Deployment was successful*"}
            """);

        highlight(value("/flows/main[0]/set/slackMessageTitle"))
                .contains(
                        ConcordHighlightingColors.EXPRESSION,
                        """
                        ${hasVariable('snapshotVersion') ?
                          "*Deployment was successful*" :
                          "*Deployment was successful*"}
                        """
                );
    }

    @Test
    public void testBlockScalarLiteralPipe() {
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

        highlight(value("/flows/main[0]/log"))
                .containsExactly(ConcordHighlightingColors.EXPRESSION, 2);
    }
}
