package brig.concord.smart;

import brig.concord.ConcordYamlTestBase;
import brig.concord.inspection.MissingKeysInspection;
import brig.concord.inspection.UnknownKeysInspection;
import brig.concord.inspection.ValueInspection;
import brig.concord.psi.FlowDocParameter;
import brig.concord.psi.FlowDocumentation;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        String yaml = """
            flows:
              ##
              # Deletes a Kubernetes manifest file using `kubectl delete`.<caret>
              # in:
              #   file: string, mandatory, k8s manifest file to delete
              ##
              default:
                - log: "Hello"
            """;

        myFixture.configureByText("test.concord.yml", yaml);

        // Press Enter
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER);

        // Check no errors
        List<HighlightInfo> errors = myFixture.doHighlighting().stream()
                .filter(info -> info.getSeverity().compareTo(HighlightSeverity.ERROR) >= 0)
                .toList();
        assertTrue("Should have no errors, but found: " + errors, errors.isEmpty());

        // Check that FlowDocumentation is still present and valid
        var file = myFixture.getFile();
        FlowDocumentation doc = PsiTreeUtil.findChildOfType(file, FlowDocumentation.class);
        assertNotNull("Should have FlowDocumentation", doc);

        ReadAction.run(() -> {
            // Check description contains original text
            String description = doc.getDescription();
            assertNotNull("Description should not be null", description);
            assertTrue("Description should contain kubectl delete text",
                    description.contains("kubectl delete"));

            // Check flow name
            assertEquals("Flow name should be 'default'", "default", doc.getFlowName());

            // Check parameters
            List<FlowDocParameter> inputs = doc.getInputParameters();
            assertEquals("Should have 1 input parameter", 1, inputs.size());

            FlowDocParameter fileParam = inputs.get(0);
            assertEquals("Parameter name", "file", fileParam.getName());
            assertEquals("Parameter type", "string", fileParam.getType());
            assertTrue("Should be mandatory", fileParam.isMandatory());
            assertEquals("Parameter description", "k8s manifest file to delete", fileParam.getDescription());
        });
    }

    @Test
    public void testEnterInDescriptionUnclosed() {
        String yaml = """
            flows:
              ##
              # Deletes a Kubernetes manifest file using `kubectl delete`.<caret>
              # in:
              #   file: string, mandatory, k8s manifest file to delete
              #
              default:
                - log: "Hello"
            """;

        myFixture.configureByText("test.concord.yml", yaml);

        // Press Enter
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER);

        // Check no errors
        List<HighlightInfo> errors = myFixture.doHighlighting().stream()
                .filter(info -> info.getSeverity().compareTo(HighlightSeverity.ERROR) >= 0)
                .toList();
        assertTrue("Should have no errors, but found: " + errors, errors.isEmpty());

        // Check that FlowDocumentation is still present and valid
        var file = myFixture.getFile();
        FlowDocumentation doc = PsiTreeUtil.findChildOfType(file, FlowDocumentation.class);
        assertNotNull("Should have FlowDocumentation", doc);

        ReadAction.run(() -> {
            // Check description contains original text
            String description = doc.getDescription();
            assertNotNull("Description should not be null", description);
            assertTrue("Description should contain kubectl delete text",
                    description.contains("kubectl delete"));

            // Check flow name
            assertEquals("Flow name should be 'default'", "default", doc.getFlowName());

            // Check parameters
            List<FlowDocParameter> inputs = doc.getInputParameters();
            assertEquals("Should have 1 input parameter", 1, inputs.size());

            FlowDocParameter fileParam = inputs.get(0);
            assertEquals("Parameter name", "file", fileParam.getName());
            assertEquals("Parameter type", "string", fileParam.getType());
            assertTrue("Should be mandatory", fileParam.isMandatory());
            assertEquals("Parameter description", "k8s manifest file to delete", fileParam.getDescription());
        });
    }


    @Test
    public void testEnterAfterSectionHeader() {
        String yaml = """
            flows:
              ##
              # Process files
              # in:<caret>
              #   file: string, mandatory, file path
              ##
              process:
                - log: "test"
            """;

        myFixture.configureByText("test.concord.yml", yaml);

        // Press Enter
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER);

        // Check no errors
        List<HighlightInfo> errors = myFixture.doHighlighting().stream()
                .filter(info -> info.getSeverity().compareTo(HighlightSeverity.ERROR) >= 0)
                .toList();
        assertTrue("Should have no errors, but found: " + errors, errors.isEmpty());

        // Check FlowDocumentation
        var file = myFixture.getFile();
        FlowDocumentation doc = PsiTreeUtil.findChildOfType(file, FlowDocumentation.class);
        assertNotNull("Should have FlowDocumentation", doc);

        ReadAction.run(() -> {
            assertEquals("Flow name should be 'process'", "process", doc.getFlowName());
            assertEquals("Description should be 'Process files'", "Process files", doc.getDescription());

            List<FlowDocParameter> inputs = doc.getInputParameters();
            assertEquals("Should have 1 input parameter", 1, inputs.size());
            assertEquals("Parameter name", "file", inputs.get(0).getName());
        });
    }

    @Test
    public void testEnterAtStartMarker() {
        String yaml = """
            flows:
              ##<caret>
              # Delete files
              # in:
              #   path: string, mandatory, file path
              ##
              delete:
                - log: "delete"
            """;

        myFixture.configureByText("test.concord.yml", yaml);

        // Press Enter
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER);

        // Check no errors
        List<HighlightInfo> errors = myFixture.doHighlighting().stream()
                .filter(info -> info.getSeverity().compareTo(HighlightSeverity.ERROR) >= 0)
                .toList();
        assertTrue("Should have no errors, but found: " + errors, errors.isEmpty());

        // Check FlowDocumentation
        var file = myFixture.getFile();
        FlowDocumentation doc = PsiTreeUtil.findChildOfType(file, FlowDocumentation.class);
        assertNotNull("Should have FlowDocumentation", doc);

        ReadAction.run(() -> {
            assertEquals("Flow name should be 'delete'", "delete", doc.getFlowName());
            List<FlowDocParameter> inputs = doc.getInputParameters();
            assertEquals("Should have 1 input parameter", 1, inputs.size());
        });
    }

    @Test
    public void testMultilineDescriptionAfterEnter() {
        String yaml = """
            flows:
              ##
              # First line of description.<caret>
              # in:
              #   param: string, mandatory
              ##
              myFlow:
                - log: "test"
            """;

        myFixture.configureByText("test.concord.yml", yaml);

        // Press Enter and type second line
        myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER);
        myFixture.type("Second line of description.");

        // Check no errors
        List<HighlightInfo> errors = myFixture.doHighlighting().stream()
                .filter(info -> info.getSeverity().compareTo(HighlightSeverity.ERROR) >= 0)
                .toList();
        assertTrue("Should have no errors, but found: " + errors, errors.isEmpty());

        // Check FlowDocumentation
        var file = myFixture.getFile();
        FlowDocumentation doc = PsiTreeUtil.findChildOfType(file, FlowDocumentation.class);
        assertNotNull("Should have FlowDocumentation", doc);

        ReadAction.run(() -> {
            String description = doc.getDescription();
            assertNotNull("Description should not be null", description);
            assertTrue("Description should contain first line",
                    description.contains("First line of description"));
            assertTrue("Description should contain second line",
                    description.contains("Second line of description"));
        });
    }
}
