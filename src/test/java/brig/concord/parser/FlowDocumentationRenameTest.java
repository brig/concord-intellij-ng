// SPDX-License-Identifier: Apache-2.0
package brig.concord.parser;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.psi.FlowDocumentation;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.util.PsiTreeUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FlowDocumentationRenameTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testRenameParameter() {
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
        Assertions.assertNotNull(doc);

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var param = doc.findParameter("oldName");
            Assertions.assertNotNull(param);

            param.setName("newName");

            Assertions.assertEquals("newName", param.getName());
            Assertions.assertEquals("string", param.getType());
            Assertions.assertTrue(param.isMandatory());
            Assertions.assertEquals("Description", param.getDescription());
        });
    }
}
