package brig.concord.completion;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElVariableCompletionTest extends ConcordYamlTestBaseJunit5 {

    private static final List<String> ALL_VARIABLES = List.of(
            "context", "execution", "txId", "workDir",
            "initiator", "currentUser", "requestInfo",
            "projectInfo", "processInfo", "tasks",
            "parentInstanceId", "currentFlowName"
    );

    @Test
    void testCompletionInEmptyExpression() {
        configureFromText("""
            flows:
              myFlow:
                - set:
                    x: ${<caret>}
            """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookups);
        assertThat(lookups).containsAll(ALL_VARIABLES);
    }

    @Test
    void testCompletionWithPrefix() {
        configureFromText("""
            flows:
              myFlow:
                - set:
                    x: ${con<caret>}
            """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).contains("context");
            assertThat(lookups).doesNotContain("txId", "workDir");
        }
    }

    @Test
    void testNoCompletionAfterDot() {
        configureFromText("""
            flows:
              myFlow:
                - set:
                    x: ${context.<caret>}
            """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).doesNotContainAnyElementsOf(ALL_VARIABLES);
        }
    }

    @Test
    void testNoCompletionOutsideExpression() {
        configureFromText("""
            flows:
              myFlow:
                - log: "<caret>"
            """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).doesNotContainAnyElementsOf(ALL_VARIABLES);
        }
    }

    @Test
    void testCompletionInSetStepValue() {
        configureFromText("""
            flows:
              myFlow:
                - set:
                    result: ${tx<caret>}
            """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).contains("txId");
        }
    }
}
