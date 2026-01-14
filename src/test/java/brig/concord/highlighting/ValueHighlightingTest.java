package brig.concord.highlighting;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class ValueHighlightingTest extends HighlightingTestBase {

    @Test
    public void testText() {
        var file = configureFromText("""
            flows
            """);

        var docs = file.getDocuments();
        var element = docs.getFirst().getTopLevelValue();

        highlight(new AbstractTarget("/") {
            @Override
            public @NotNull String text() {
                return ReadAction.compute(() -> element.getText());
            }

            @Override
            public @NotNull TextRange range() {
                return ReadAction.compute(() -> element.getTextRange());
            }
        }).isOnly(ConcordHighlightingColors.STRING);
    }

    // ==================== Boolean Values ====================

    @Test
    public void testTrueValue() {
        configureFromText("""
            flows:
              main:
                - task: "http"
                  ignoreErrors: true
            """);

        highlight(value("/flows/main[0]/ignoreErrors")).is(ConcordHighlightingColors.BOOLEAN);
    }

    @Test
    public void testFalseValue() {
        configureFromText("""
            configuration:
              debug: false
            """);

        highlight(value("/configuration/debug")).is(ConcordHighlightingColors.BOOLEAN);
    }

    @Test
    public void testMultipleBooleans() {
        configureFromText("""
            flows:
              main:
                - task: "http"
                  in:
                    followRedirects: true
                    throwOnError: false
                  ignoreErrors: true
            """);

        highlight(value("/flows/main[0]/in/followRedirects")).is(ConcordHighlightingColors.BOOLEAN);
        highlight(value("/flows/main[0]/in/throwOnError")).is(ConcordHighlightingColors.BOOLEAN);
    }

    // ==================== Null Values ====================

    @Test
    public void testNullValue() {
        configureFromText("""
            configuration:
              arguments:
                optionalParam: null
            """);

        highlight(value("/configuration/arguments/optionalParam")).is(ConcordHighlightingColors.NULL);
    }

    @Test
    public void testTildeAsNull() {
        configureFromText("""
            configuration:
              arguments:
                emptyParam: ~
            """);

        highlight(value("/configuration/arguments/emptyParam")).is(ConcordHighlightingColors.NULL);
    }

    // ==================== Mixed Values ====================

    @Test
    public void testMixedValuesInStep() {
        configureFromText("""
            flows:
              main:
                - task: "http"
                  in:
                    url: ${apiUrl}
                    timeout: 30
                    followRedirects: true
                    body: null
                  ignoreErrors: false
            """);

        // Expression
        highlight(value("/flows/main[0]/in/url")).is(ConcordHighlightingColors.EXPRESSION);

        // Booleans
        highlight(value("/flows/main[0]/in/followRedirects")).is(ConcordHighlightingColors.BOOLEAN);
        highlight(value("/flows/main[0]/ignoreErrors")).is(ConcordHighlightingColors.BOOLEAN);

        // Null
        highlight(value("/flows/main[0]/in/body")).is(ConcordHighlightingColors.NULL);
    }

    @Test
    public void testExpressionNotConfusedWithString() {
        // A string that looks like expression syntax but isn't
        configureFromText("""
            flows:
              main:
                - log: "Not an expression: $notVar"
            """);

        // $notVar should NOT be highlighted as expression (it's not ${...} pattern)
        highlight(value("flows/main[0]/log").substring("$notVar"))
                .is(ConcordHighlightingColors.STRING);

        highlight(value("flows/main[0]/log").substring("$notVar"))
                .isNot(ConcordHighlightingColors.EXPRESSION);

        // The whole string should not be an expression either
        highlight(value("/flows/main[0]/log")).is(ConcordHighlightingColors.STRING);
    }

    @Test
    public void testNestedExpressions() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: ${map.get(key)}
                - if: ${result != null && result.success}
                  then:
                    - log: ${result.message}
            """);

        highlight(value("/flows/main[0]/set/result")).is(ConcordHighlightingColors.EXPRESSION);
        highlight(value("/flows/main[1]/if")).is(ConcordHighlightingColors.EXPRESSION);
        highlight(value("/flows/main[1]/then[0]/log")).is(ConcordHighlightingColors.EXPRESSION);
    }

    @Test
    public void testStringValue() {
        configureFromText("""
            flows:
              main:
                - set:
                    str: abc
            """);

        highlight(value("/flows/main[0]/set/str")).is(ConcordHighlightingColors.STRING);
    }

    @Test
    public void testQStringValue() {
        configureFromText("""
            flows:
              main:
                - set:
                    str: "abc"
            """);

        highlight(value("/flows/main[0]/set/str")).is(ConcordHighlightingColors.STRING);
    }

    @Test
    public void testMStringValue() {
        configureFromText("""
            flows:
              main:
                - set:
                    str: |
                      abc
            """);

        highlight(value("/flows/main[0]/set/str")).is(ConcordHighlightingColors.STRING);
    }

    @Test
    public void testNumValue() {
        configureFromText("""
            flows:
              main:
                - set:
                    num: 123
            """);

        highlight(value("/flows/main[0]/set/num")).is(ConcordHighlightingColors.NUMBER);
    }

    @Test
    public void testSingleQuotedStringValue() {
        configureFromText("""
            configuration:
              runtime: 'concord-v2'
            """);

        highlight(value("/configuration/runtime")).is(ConcordHighlightingColors.STRING);
    }
}
