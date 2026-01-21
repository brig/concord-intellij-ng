package brig.concord.inspection.fix;

import brig.concord.ConcordYamlTestBase;
import brig.concord.assertions.FlowDocAssertions;
import brig.concord.assertions.InspectionAssertions;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.testFramework.EdtTestUtil;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class InsertClosingMarkerFixTest extends ConcordYamlTestBase {

    @Test
    public void testQuickFixForUnclosedFlowDoc() {
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
        assertEquals(1, errors.size());
        assertEquals("Expected closing ## marker", errors.getFirst().getDescription());

        // Move caret to error location
        EdtTestUtil.runInEdtAndWait(() ->
                myFixture.getEditor().getCaretModel().moveToOffset(errors.getFirst().getStartOffset()));

        // Apply quick fix
        var intentions = myFixture.filterAvailableIntentions("Insert closing ## marker");
        assertFalse("Quick fix should be available", intentions.isEmpty());
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
    public void testQuickFixPreview() {
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
        assertFalse("Quick fix should be available", intentions.isEmpty());

        String preview = myFixture.getIntentionPreviewText(intentions.getFirst());
        assertNotNull("Preview should be available", preview);
        assertTrue("Preview should contain closing marker", preview.contains("##\n"));
    }

    private void assertFlowDoc(KeyTarget flowKey, Consumer<FlowDocAssertions> assertions) {
        FlowDocAssertions.assertFlowDoc(yamlPath, flowKey, assertions);
    }
}
