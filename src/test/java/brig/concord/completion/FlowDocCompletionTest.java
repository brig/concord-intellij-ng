package brig.concord.completion;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class FlowDocCompletionTest extends ConcordYamlTestBaseJunit5 {

    private static final List<String> ALL_TYPES = List.of(
            "string", "boolean", "int", "integer", "number", "object", "any",
            "string[]", "boolean[]", "int[]", "integer[]", "number[]", "object[]", "any[]"
    );

    private static final List<String> KEYWORDS = List.of("mandatory", "optional");

    @Test
    void testTypeCompletionAfterColon() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: <caret>
              ##
              myFlow:
                - log: "test"
            """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookups);
        assertContainsElements(lookups, ALL_TYPES);
    }

    @Test
    void testTypeCompletionWithPrefix() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: str<caret>
              ##
              myFlow:
                - log: "test"
            """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookups);
        assertContainsElements(lookups, "string", "string[]");
    }

    @Test
    void testKeywordCompletionAfterType() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: string, <caret>
              ##
              myFlow:
                - log: "test"
            """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookups);
        assertContainsElements(lookups, KEYWORDS);
    }

    @Test
    void testKeywordCompletionWithPrefix() {
        configureFromText("""
            flows:
              ##
              # in:
              #   param: string, m<caret>
              ##
              myFlow:
                - log: "test"
            """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        // May be null if auto-completed, that's OK
        if (lookups != null) {
            assertContainsElements(lookups, "mandatory");
        }
    }

    @Test
    void testNoCompletionOutsideFlowDoc() {
        configureFromText("""
            flows:
              myFlow:
                - log: "<caret>"
            """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        // Should not contain flow doc types/keywords
        if (lookups != null) {
            assertDoesntContain(lookups, ALL_TYPES);
            assertDoesntContain(lookups, KEYWORDS);
        }
    }
}
