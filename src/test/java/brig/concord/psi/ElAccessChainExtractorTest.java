package brig.concord.psi;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.el.psi.ElMemberName;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ElAccessChainExtractorTest extends ConcordYamlTestBaseJunit5 {

    private List<String> extractChain() {
        var offset = myFixture.getCaretOffset();
        var leaf = myFixture.getFile().findElementAt(offset);
        assertNotNull(leaf, "Should find element at caret");

        var memberName = PsiTreeUtil.getParentOfType(leaf, ElMemberName.class, false);
        assertNotNull(memberName, "Should find ElMemberName at caret");

        return ElAccessChainExtractor.extractChainSegments(memberName);
    }

    @Test
    void testSimplePropertyAccess() {
        configureFromText("""
                flows:
                  main:
                    - log: "${obj.<caret>prop}"
                """);

        assertThat(extractChain()).isEqualTo(List.of("obj"));
    }

    @Test
    void testTwoLevelPropertyAccess() {
        configureFromText("""
                flows:
                  main:
                    - log: "${obj.p1.<caret>prop}"
                """);

        assertThat(extractChain()).isEqualTo(List.of("obj", "p1"));
    }

    @Test
    void testThreeLevelPropertyAccess() {
        configureFromText("""
                flows:
                  main:
                    - log: "${obj.p1.p2.<caret>prop}"
                """);

        assertThat(extractChain()).isEqualTo(List.of("obj", "p1", "p2"));
    }

    @Test
    void testBracketWithStringLiteral() {
        configureFromText("""
                flows:
                  main:
                    - log: "${obj['p1'].<caret>prop}"
                """);

        assertThat(extractChain()).isEqualTo(List.of("obj", "p1"));
    }

    @Test
    void testBracketWithDoubleQuotedString() {
        configureFromText("""
                flows:
                  main:
                    - log: '${obj["p1"].<caret>prop}'
                """);

        assertThat(extractChain()).isEqualTo(List.of("obj", "p1"));
    }

    @Test
    void testMixedDotAndBracketAccess() {
        configureFromText("""
                flows:
                  main:
                    - log: "${obj['p1'].p2.<caret>prop}"
                """);

        assertThat(extractChain()).isEqualTo(List.of("obj", "p1", "p2"));
    }

    @Test
    void testBracketWithNonStringBreaksChain() {
        configureFromText("""
                flows:
                  main:
                    - log: "${obj[0].<caret>prop}"
                """);

        assertThat(extractChain()).isEmpty();
    }

    @Test
    void testBracketWithVariableBreaksChain() {
        configureFromText("""
                flows:
                  main:
                    - log: "${obj[key].<caret>prop}"
                """);

        assertThat(extractChain()).isEmpty();
    }

    @Test
    void testNonIdentifierBaseBreaksChain() {
        configureFromText("""
                flows:
                  main:
                    - log: "${(a + b).<caret>prop}"
                """);

        assertThat(extractChain()).isEmpty();
    }

    @Test
    void testMethodCallBreaksChain() {
        configureFromText("""
                flows:
                  main:
                    - log: "${obj.method().<caret>prop}"
                """);

        assertThat(extractChain()).isEmpty();
    }

    @Test
    void testPlainTextExpression() {
        configureFromText("""
                flows:
                  main:
                    - if: ${obj.<caret>prop}
                """);

        assertThat(extractChain()).isEqualTo(List.of("obj"));
    }

    // --- extractFullChain tests ---

    @Test
    void testExtractFullChainSimple() {
        configureFromText("""
                configuration:
                  arguments:
                    myUser: "${initiator}"
                flows:
                  main:
                    - log: "${myUser.<caret>x}"
                """);

        var scalar = value("/configuration/arguments/myUser").element();

        assertThat(ElAccessChainExtractor.extractFullChain(scalar)).isEqualTo(List.of("initiator"));
    }

    @Test
    void testExtractFullChainDotAccess() {
        configureFromText("""
                configuration:
                  arguments:
                    x: "${obj.inner}"
                flows:
                  main:
                    - log: "${x.<caret>y}"
                """);

        var scalar = value("/configuration/arguments/x").element();
        assertThat(ElAccessChainExtractor.extractFullChain(scalar)).isEqualTo(List.of("obj", "inner"));
    }

    @Test
    void testExtractFullChainMixedContent() {
        configureFromText("""
                configuration:
                  arguments:
                    x: "prefix ${initiator}"
                flows:
                  main:
                    - log: "${x.<caret>y}"
                """);

        var scalar = value("/configuration/arguments/x").element();
        assertThat(ElAccessChainExtractor.extractFullChain(scalar)).isNull();
    }

    @Test
    void testExtractFullChainPlainScalar() {
        configureFromText("""
                configuration:
                  arguments:
                    x: "hello"
                flows:
                  main:
                    - log: "${x.<caret>y}"
                """);

        var scalar = value("/configuration/arguments/x").element();
        assertThat(ElAccessChainExtractor.extractFullChain(scalar)).isNull();
    }
}
