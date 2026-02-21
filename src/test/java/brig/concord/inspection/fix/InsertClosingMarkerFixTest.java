// SPDX-License-Identifier: Apache-2.0
package brig.concord.inspection.fix;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.assertions.InspectionAssertions;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.testFramework.EdtTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class InsertClosingMarkerFixTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testQuickFixForUnclosedFlowDoc() {
        configureFromText("""
            flows:
              ##
              # Description
              # in:
              #   param1: string, mandatory
              <caret>default:
                - log: "Hello"
            """);

        // Verify error exists and get its offset
        var errors = myFixture.doHighlighting().stream()
                .filter(info -> info.getSeverity().compareTo(HighlightSeverity.ERROR) >= 0)
                .toList();
        Assertions.assertEquals(1, errors.size());
        Assertions.assertEquals("Expected closing ## marker", errors.getFirst().getDescription());

        // Move caret to error location
        EdtTestUtil.runInEdtAndWait(() ->
                myFixture.getEditor().getCaretModel().moveToOffset(errors.getFirst().getStartOffset()));

        // Apply quick fix
        var intentions = myFixture.filterAvailableIntentions("Insert closing ## marker");
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should be available");
        myFixture.launchAction(intentions.getFirst());

        // Verify the resulting text
        myFixture.checkResult("""
            flows:
              ##
              # Description
              # in:
              #   param1: string, mandatory
              ##
              default:
                - log: "Hello"
            """);

        // Verify no more errors
        InspectionAssertions.assertNoErrors(this.myFixture);

        // Verify the flow doc is now properly parsed
        assertFlowDoc(key("/flows/default"), doc -> doc
                .hasFlowName("default")
                .hasInputCount(1)
                .param("param1").hasType("string").isMandatory());
    }

    @Test
    void testQuickFixPreview() {
        configureFromText("""
            flows:
              ##
              # Description
              # in:
              #   param1: string, mandatory
              <caret>default:
                - log: "Hello"
            """);

        var intentions = myFixture.filterAvailableIntentions("Insert closing ## marker");
        Assertions.assertFalse(intentions.isEmpty(), "Quick fix should be available");

        String preview = myFixture.getIntentionPreviewText(intentions.getFirst());
        Assertions.assertNotNull(preview, "Preview should be available");
        Assertions.assertTrue(preview.contains("##\n"), "Preview should contain closing marker");
    }

}
