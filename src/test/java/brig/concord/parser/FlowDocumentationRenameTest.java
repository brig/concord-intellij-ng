package brig.concord.parser;

import brig.concord.ConcordYamlTestBase;
import brig.concord.psi.FlowDocumentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Test;

public class FlowDocumentationRenameTest extends ConcordYamlTestBase {

    @Test
    public void testRenameParameter() {
        var yaml = """
            flows:
              ##
              # in:
              #   oldName: string, mandatory, Description
              ##
              testFlow:
                - log: "test"
            """;

        var file = configureFromText(yaml);
        var doc = PsiTreeUtil.findChildOfType(file, FlowDocumentation.class);
        assertNotNull(doc);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var param = doc.findParameter("oldName");
            assertNotNull(param);

            param.setName("newName");

            assertEquals("newName", param.getName());
            assertEquals("string", param.getType());
            assertTrue(param.isMandatory());
            assertEquals("Description", param.getDescription());
        });
    }
}
