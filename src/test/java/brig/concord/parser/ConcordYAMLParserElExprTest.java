// SPDX-License-Identifier: Apache-2.0
package brig.concord.parser;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.lexer.ConcordElTokenTypes;
import brig.concord.yaml.YAMLElementTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.ParsingTestUtil;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ConcordYAMLParser's EL expression handling:
 * parsing ${...} into EL_EXPR_START + EL_EXPR (collapsed) + EL_EXPR_END nodes.
 */
class ConcordYAMLParserElExprTest extends ConcordYamlTestBaseJunit5 {

    // --- Plain text expressions ---

    @Test
    void plainTextExpression() {
        var file = parse("- log: ${expr}");

        assertNoPsiErrors(file);
        assertElExpressionCount(file, 1);
        assertHasElExpression(file, "expr");
        assertScalarType(file, YAMLElementTypes.SCALAR_PLAIN_VALUE);
    }

    @Test
    void plainTextExpressionStartingValue() {
        var file = parse("- if: ${condition}");

        assertNoPsiErrors(file);
        assertElExpressionCount(file, 1);
        assertHasElExpression(file, "condition");
        assertScalarType(file, YAMLElementTypes.SCALAR_PLAIN_VALUE);
    }

    @Test
    void emptyExpression() {
        var file = parse("- log: ${}");

        assertNoPsiErrors(file);
        // Empty expression: EL_EXPR_START + EL_EXPR_END, no EL_EXPRESSION body
        assertElExpressionCount(file, 0);
        assertTokenCount(file, ConcordElTokenTypes.EL_EXPR_START, 1);
        assertTokenCount(file, ConcordElTokenTypes.EL_EXPR_END, 1);
    }

    // --- Quoted string expressions ---

    @Test
    void doubleQuotedStringExpression() {
        var file = parse("key: \"pre ${x} post\"");

        assertNoPsiErrors(file);
        assertElExpressionCount(file, 1);
        assertHasElExpression(file, "x");
        assertScalarType(file, YAMLElementTypes.SCALAR_QUOTED_STRING);
    }

    @Test
    void singleQuotedStringExpression() {
        var file = parse("key: '${expr}'");

        assertNoPsiErrors(file);
        assertElExpressionCount(file, 1);
        assertHasElExpression(file, "expr");
        assertScalarType(file, YAMLElementTypes.SCALAR_QUOTED_STRING);
    }

    @Test
    void quotedStringMultipleExpressions() {
        var file = parse("key: \"${a} ${b}\"");

        assertNoPsiErrors(file);
        assertElExpressionCount(file, 2);
        assertHasElExpression(file, "a");
        assertHasElExpression(file, "b");
        assertTokenCount(file, ConcordElTokenTypes.EL_EXPR_START, 2);
        assertTokenCount(file, ConcordElTokenTypes.EL_EXPR_END, 2);
    }

    @Test
    void quotedStringAdjacentExpressions() {
        var file = parse("key: \"${a}${b}\"");

        assertNoPsiErrors(file);
        assertElExpressionCount(file, 2);
        assertTokenCount(file, ConcordElTokenTypes.EL_EXPR_START, 2);
        assertTokenCount(file, ConcordElTokenTypes.EL_EXPR_END, 2);
    }

    @Test
    void escapedExpressionNotParsed() {
        var file = parse("key: \"\\${escaped}\"");

        assertNoPsiErrors(file);
        // Escaped ${ is not split by the lexer, so no EL nodes
        assertElExpressionCount(file, 0);
        assertTokenCount(file, ConcordElTokenTypes.EL_EXPR_START, 0);
    }

    @Test
    void yamlEscapedQuoteInsideElStringInDoubleQuotedYamlString() {
        // YAML: - log: "${\"No GitHub branch named '\"}"
        // After YAML unescaping: ${"No GitHub branch named '"}
        // This is a valid EL expression containing a double-quoted string literal.
        // The \" sequences are YAML escapes, not EL escapes.
        var file = parse("- log: \"${\\\"No GitHub branch named '\\\"}\"\n");

        assertNoPsiErrors(file);
        assertElExpressionCount(file, 1);
    }

    // --- Block scalar expressions ---

    @Test
    void literalBlockScalarSingleLineExpression() {
        var file = parse("""
            key: |
              text ${a} more""");

        assertNoPsiErrors(file);
        assertElExpressionCount(file, 1);
        assertHasElExpression(file, "a");
        assertScalarType(file, YAMLElementTypes.SCALAR_LIST_VALUE);
    }

    @Test
    void literalBlockScalarMultiLineExpression() {
        var file = parse("""
            key: |
              text ${a +
              b} more""");

        assertNoPsiErrors(file);
        // Multi-line expression: body tokens (EL_EXPR_BODY, SCALAR_EOL, INDENT)
        // are collapsed into a single EL_EXPRESSION chameleon node
        assertElExpressionCount(file, 1);
        // The collapsed node contains the full multi-line expression text
        var exprNodes = findNodes(file, ConcordElTokenTypes.EL_EXPR);
        assertEquals(1, exprNodes.size());
        assertTrue(exprNodes.getFirst().getText().contains("a +"),
                "EL_EXPRESSION should contain 'a +': " + exprNodes.getFirst().getText());
        assertTrue(exprNodes.getFirst().getText().contains("b"),
                "EL_EXPRESSION should contain 'b': " + exprNodes.getFirst().getText());
        assertScalarType(file, YAMLElementTypes.SCALAR_LIST_VALUE);
    }

    @Test
    void literalBlockScalarThreeLineExpression() {
        var file = parse("""
            key: |
              ${a +
              b +
              c}""");

        assertNoPsiErrors(file);
        assertElExpressionCount(file, 1);
        var exprNodes = findNodes(file, ConcordElTokenTypes.EL_EXPR);
        var text = exprNodes.getFirst().getText();
        assertTrue(text.contains("a +"), "Should contain 'a +': " + text);
        assertTrue(text.contains("b +"), "Should contain 'b +': " + text);
        assertTrue(text.contains("c"), "Should contain 'c': " + text);
    }

    @Test
    void foldedBlockScalarMultiLineExpression() {
        var file = parse("""
            key: >
              text ${a +
              b} more""");

        assertNoPsiErrors(file);
        assertElExpressionCount(file, 1);
        assertScalarType(file, YAMLElementTypes.SCALAR_TEXT_VALUE);
        var exprNodes = findNodes(file, ConcordElTokenTypes.EL_EXPR);
        assertTrue(exprNodes.getFirst().getText().contains("a +"));
    }

    // --- Combined scenario (from docs-site example) ---

    @Test
    void mixedExpressionsInFlow() {
        var file = parse("""
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
                    - log: \\${escaped} is plain text
            """);

        assertNoPsiErrors(file);
        // "Hello, ${userName}!" — 1 expression in quoted string
        // ${status == "active"} — 1 expression in plain text
        // ${hasVariable('env') ? ... } — 1 multi-line expression in block scalar
        // \${escaped} — escaped, no expression
        assertElExpressionCount(file, 3);
    }

    // --- Unclosed expression errors ---

    @Test
    void unclosedExpressionInPlainText() {
        var file = parse("- log: ${expr");

        assertHasPsiError(file, "Unclosed expression: missing '}'");
        assertElExpressionCount(file, 1);
        assertHasElExpression(file, "expr");
    }

    @Test
    void unclosedExpressionInQuotedString() {
        var file = parse("key: \"pre ${x post\"");

        assertHasPsiError(file, "Unclosed expression: missing '}'");
    }

    @Test
    void unclosedExpressionInSingleQuotedString() {
        var file = parse("key: '${unclosed'");

        assertHasPsiError(file, "Unclosed expression: missing '}'");
    }

    @Test
    void unclosedExpressionInBlockScalar() {
        var file = parse("""
            key: |
              ${hasVariable('env') ?
              "Production" :
              "Development"
            other: value""");

        assertHasPsiError(file, "Unclosed expression: missing '}'");
    }

    @Test
    void unclosedEmptyExpression() {
        var file = parse("- log: ${");

        assertHasPsiError(file, "Unclosed expression: missing '}'");
    }

    // --- helpers ---

    private com.intellij.psi.PsiFile parse(String yaml) {
        var file = configureFromText("a.concord.yaml", yaml);
        // Force lazy parsing of all chameleon nodes
        file.accept(new PsiRecursiveElementWalkingVisitor() {});
        return file;
    }

    private void assertNoPsiErrors(com.intellij.psi.PsiFile file) {
        ReadAction.run(() -> ParsingTestUtil.assertNoPsiErrorElements(file));
    }

    private void assertHasPsiError(com.intellij.psi.PsiFile file, String expectedMessage) {
        ReadAction.run(() -> {
            var errors = new ArrayList<PsiErrorElement>();
            file.accept(new PsiRecursiveElementWalkingVisitor() {
                @Override
                public void visitErrorElement(@org.jetbrains.annotations.NotNull PsiErrorElement element) {
                    errors.add(element);
                    super.visitErrorElement(element);
                }
            });
            assertFalse(errors.isEmpty(), "Expected PSI error but none found");
            assertTrue(errors.stream().anyMatch(e -> e.getErrorDescription().contains(expectedMessage)),
                    "Expected error containing '" + expectedMessage + "', found: " +
                            errors.stream().map(PsiErrorElement::getErrorDescription).toList());
        });
    }

    private void assertElExpressionCount(com.intellij.psi.PsiFile file, int expected) {
        var nodes = findNodes(file, ConcordElTokenTypes.EL_EXPR);
        assertEquals(expected, nodes.size(),
                "Expected " + expected + " EL_EXPRESSION nodes, found " + nodes.size());
    }

    private void assertHasElExpression(com.intellij.psi.PsiFile file, String containsText) {
        var nodes = findNodes(file, ConcordElTokenTypes.EL_EXPR);
        assertTrue(nodes.stream().anyMatch(n -> n.getText().contains(containsText)),
                "No EL_EXPRESSION containing '" + containsText + "' found. Expressions: " +
                        nodes.stream().map(ASTNode::getText).toList());
    }

    private void assertScalarType(com.intellij.psi.PsiFile file, IElementType expected) {
        var nodes = findNodes(file, expected);
        assertFalse(nodes.isEmpty(), "Expected at least one " + expected + " node");
    }

    private void assertTokenCount(com.intellij.psi.PsiFile file, IElementType type, int expected) {
        var nodes = findNodes(file, type);
        assertEquals(expected, nodes.size(),
                "Expected " + expected + " " + type + " tokens, found " + nodes.size());
    }

    private List<ASTNode> findNodes(com.intellij.psi.PsiFile file, IElementType type) {
        return ReadAction.compute(() -> {
            var result = new ArrayList<ASTNode>();
            collectNodes(file.getNode(), type, result);
            return result;
        });
    }

    private static void collectNodes(ASTNode node, IElementType type, List<ASTNode> result) {
        if (node.getElementType() == type) {
            result.add(node);
        }
        for (var child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            collectNodes(child, type, result);
        }
    }
}
