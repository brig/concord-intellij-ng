package brig.concord.smart;

import brig.concord.ConcordYamlTestBase;
import brig.concord.assertions.FlowDocAssertions;
import brig.concord.assertions.InspectionAssertions;
import brig.concord.inspection.MissingKeysInspection;
import brig.concord.inspection.UnknownKeysInspection;
import brig.concord.inspection.ValueInspection;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.IdeActions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

public class FlowDocEnterHandlerTest extends ConcordYamlTestBase {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        myFixture.enableInspections(
                MissingKeysInspection.class,
                UnknownKeysInspection.class,
                ValueInspection.class
        );
    }

    @Test
    public void testEnterInDescription() {
        configureFromText("""
            flows:
              ##
              # Deletes a Kubernetes manifest file using `kubectl delete`.<caret>
              # in:
              #   file: string, mandatory, k8s manifest file to delete
              ##
              default:
                - log: "Hello"
            """);

        pressEnter();

        InspectionAssertions.assertNoErrors(myFixture);

        assertFlowDoc(key("/flows/default"), doc -> doc
                .hasFlowName("default")
                .descriptionContains("kubectl delete")
                .hasInputCount(1)
                .param("file").hasType("string").isMandatory().hasDescription("k8s manifest file to delete"));
    }

    @Test
    public void testEnterInDescriptionUnclosed() {
        configureFromText("""
            flows:
              ##
              # Deletes a Kubernetes manifest file using `kubectl delete`.<caret>
              # in:
              #   file: string, mandatory, k8s manifest file to delete
              #
              default:
                - log: "Hello"
            """);

        pressEnter();

        var errors = myFixture.doHighlighting().stream()
                .filter(info -> info.getSeverity().compareTo(HighlightSeverity.ERROR) >= 0)
                .toList();
        assertEquals(1, errors.size());
        assertEquals("Expected closing ## marker", errors.getFirst().getDescription());

        assertFlowDoc(key("/flows/default"), doc -> doc
                .hasFlowName("default")
                .descriptionContains("kubectl delete")
                .hasInputCount(1)
                .param("file").hasType("string").isMandatory().hasDescription("k8s manifest file to delete"));
    }

    @Test
    public void testEnterAfterSectionHeader() {
        configureFromText("""
            flows:
              ##
              # Process files
              # in:<caret>
              #   file: string, mandatory, file path
              ##
              process:
                - log: "test"
            """);

        pressEnter();
        InspectionAssertions.assertNoErrors(myFixture);

        assertFlowDoc(key("/flows/process"), doc -> doc
                .hasFlowName("process")
                .hasDescription("Process files")
                .hasInputCount(1)
                .param("file").hasType("string").isMandatory().hasDescription("file path"));
    }

    @Test
    public void testEnterAtStartMarker() {
        configureFromText("""
            flows:
              ##<caret>
              # Delete files
              # in:
              #   path: string, mandatory, file path
              ##
              delete:
                - log: "delete"
            """);

        pressEnter();
        InspectionAssertions.assertNoErrors(myFixture);

        assertFlowDoc(key("/flows/delete"), doc -> doc
                .hasFlowName("delete")
                .hasInputCount(1)
                .param("path").hasType("string").isMandatory());
    }

    @Test
    public void testMultilineDescriptionAfterEnter() {
        configureFromText("""
            flows:
              ##
              # First line of description.<caret>
              # in:
              #   param: string, mandatory
              ##
              myFlow:
                - log: "test"
            """);

        pressEnter();
        myFixture.type("Second line of description.");

        InspectionAssertions.assertNoErrors(myFixture);

        assertFlowDoc(key("/flows/myFlow"), doc -> doc
                .descriptionContains("First line of description")
                .descriptionContains("Second line of description"));
    }

    private void pressEnter() {
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER);
    }

    private void assertFlowDoc(KeyTarget flowKey, Consumer<FlowDocAssertions> assertions) {
        FlowDocAssertions.assertFlowDoc(yamlPath, flowKey, assertions);
    }
}
