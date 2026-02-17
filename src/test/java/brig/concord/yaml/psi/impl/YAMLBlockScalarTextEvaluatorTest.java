// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi.impl;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.TextRange;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class YAMLBlockScalarTextEvaluatorTest extends ConcordYamlTestBaseJunit5 {

    // --- getTextValue() tests: no expressions (baseline) ---

    @Test
    void literalScalarNoExpressions() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      line1
                      line2
            """);

        assertTextValue("/flows/main[0]/set/result", "line1\nline2\n");
    }

    @Test
    void literalScalarSingleLineExpression() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      text ${expr} more
            """);

        assertTextValue("/flows/main[0]/set/result", "text ${expr} more\n");
    }

    // --- getTextValue() tests: multi-line EL expressions ---

    @Test
    void literalScalarMultiLineExpression() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      text ${a +
                      b} more
            """);

        // Multi-line EL expression: newlines inside the collapsed EL_EXPRESSION node
        // are stripped by content range splitting; literal block scalar joiner is ""
        assertTextValue("/flows/main[0]/set/result", "text ${a +b} more\n");
    }

    @Test
    void literalScalarMultiLineExpressionThreeLines() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      ${a +
                      b +
                      c}
            """);

        assertTextValue("/flows/main[0]/set/result", "${a +b +c}\n");
    }

    @Test
    void literalScalarMixedExpressions() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      ${single} and ${multi +
                      line}
            """);

        assertTextValue("/flows/main[0]/set/result", "${single} and ${multi +line}\n");
    }

    @Test
    void literalScalarExpressionWithTextAfter() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      before ${a +
                      b} after
                      next line
            """);

        assertTextValue("/flows/main[0]/set/result", "before ${a +b} after\nnext line\n");
    }

    @Test
    void foldedScalarMultiLineExpression() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: >
                      text ${a +
                      b} more
            """);

        // Folded scalars use YAMLBlockScalarTextEvaluator with space joiner,
        // so the multi-line EL expression's lines are joined with a space
        assertTextValue("/flows/main[0]/set/result", "text ${a + b} more\n");
    }

    @Test
    void literalScalarStripChomping() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |-
                      text ${a +
                      b} more
            """);

        assertTextValue("/flows/main[0]/set/result", "text ${a +b} more");
    }

    @Test
    void literalScalarKeepChomping() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |+
                      text ${a +
                      b} more
            """);

        assertTextValueStartsWith("/flows/main[0]/set/result", "text ${a +b} more\n");
    }

    @Test
    void literalScalarNestedBracesMultiLine() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      ${foo({a:
                      1})}
            """);

        assertTextValue("/flows/main[0]/set/result", "${foo({a:1})}\n");
    }

    // --- getContentRanges() tests (PSI element level) ---

    @Test
    void contentRangesNoExpressions() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      line1
                      line2
            """);

        var ranges = getContentRanges("/flows/main[0]/set/result");
        assertFalse(ranges.isEmpty());
        assertContentRangesContain("/flows/main[0]/set/result", ranges, "line1", "line2");
    }

    @Test
    void contentRangesMultiLineExpression() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      text ${a +
                      b} more
            """);

        var ranges = getContentRanges("/flows/main[0]/set/result");
        // Should have at least 2 ranges: one for first line content, one for second
        assertTrue(ranges.size() >= 2, "Expected at least 2 content ranges, got " + ranges.size());
        assertContentRangesContain("/flows/main[0]/set/result", ranges, "text ${a +", "b} more");
    }

    @Test
    void contentRangesThreeLineExpression() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      ${a +
                      b +
                      c}
            """);

        var ranges = getContentRanges("/flows/main[0]/set/result");
        assertTrue(ranges.size() >= 3, "Expected at least 3 content ranges, got " + ranges.size());
        assertContentRangesContain("/flows/main[0]/set/result", ranges, "${a +", "b +", "c}");
    }

    @Test
    void contentRangesExpressionThenPlainLine() {
        configureFromText("""
            flows:
              main:
                - set:
                    result: |
                      before ${a +
                      b} after
                      next line
            """);

        var ranges = getContentRanges("/flows/main[0]/set/result");
        assertTrue(ranges.size() >= 3, "Expected at least 3 content ranges, got " + ranges.size());
        assertContentRangesContain("/flows/main[0]/set/result", ranges,
                "before ${a +", "b} after", "next line");
    }

    // --- helpers ---

    private void assertTextValue(String path, String expected) {
        var scalar = ReadAction.compute(() -> {
            var element = value(path).element();
            assertInstanceOf(YAMLScalarImpl.class, element, "Expected YAMLScalarImpl at " + path);
            return (YAMLScalarImpl) element;
        });
        var actual = ReadAction.compute(scalar::getTextValue);
        assertEquals(expected, actual);
    }

    private void assertTextValueStartsWith(String path, String prefix) {
        var scalar = ReadAction.compute(() -> {
            var element = value(path).element();
            assertInstanceOf(YAMLScalarImpl.class, element, "Expected YAMLScalarImpl at " + path);
            return (YAMLScalarImpl) element;
        });
        var actual = ReadAction.compute(scalar::getTextValue);
        assertTrue(actual.startsWith(prefix),
                "Expected text value to start with '" + escape(prefix) + "' but got '" + escape(actual) + "'");
    }

    private List<TextRange> getContentRanges(String path) {
        return ReadAction.compute(() -> {
            var element = value(path).element();
            assertInstanceOf(YAMLScalarImpl.class, element, "Expected YAMLScalarImpl at " + path);
            return ((YAMLScalarImpl) element).getContentRanges();
        });
    }

    private void assertContentRangesContain(String path, List<TextRange> ranges, String... expectedTexts) {
        var text = ReadAction.compute(() -> value(path).element().getText());
        var rangeTexts = ranges.stream()
                .map(r -> r.substring(text))
                .filter(s -> !s.isEmpty())
                .toList();
        for (String expected : expectedTexts) {
            assertTrue(rangeTexts.stream().anyMatch(r -> r.contains(expected)),
                    "Expected a content range containing '" + expected + "' but ranges are: " + rangeTexts);
        }
    }

    private static String escape(String s) {
        return s.replace("\n", "\\n").replace("\t", "\\t");
    }
}
