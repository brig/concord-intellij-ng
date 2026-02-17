// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.refactoring;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.testFramework.EdtTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RenameFlowTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testRenameFlow() {
        configureFromText("""
                flows:
                  myFlow:
                    - call: myFlow
                """);

        renameFlow("myFlow", "newFlowName");

        myFixture.checkResult("""
                flows:
                  newFlowName:
                    - call: newFlowName
                """);
    }

    @Test
    void testRenameFlowMultipleCalls() {
        configureFromText("""
                flows:
                  myFlow:
                    - log: "Step 1"
                  main:
                    - call: myFlow
                    - call: myFlow
                """);

        renameFlow("myFlow", "renamedFlow");

        myFixture.checkResult("""
                flows:
                  renamedFlow:
                    - log: "Step 1"
                  main:
                    - call: renamedFlow
                    - call: renamedFlow
                """);
    }

    @Test
    void renameInMultiFiles() {
        // Setup Project A
        var rootA = createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: utilsFlow
                """);

        var utilsA = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "Utils from project A"
                  utilsFlow2:
                    - log: "Utils2 from project A"
                """);

        configureFromExistingFile(utilsA);

        renameFlow("utilsFlow", "renamedFlow");

        myFixture.checkResult("""
                flows:
                  renamedFlow:
                    - log: "Utils from project A"
                  utilsFlow2:
                    - log: "Utils2 from project A"
                """);

        assertFileText(rootA,
                """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: renamedFlow
                """);
    }

    @Test
    void renameInMultiScopes() {
        // Setup Project A
        var rootA = createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: utilsFlow
                """);

        var utilsA = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "Utils from project A"
                  utilsFlow2:
                    - log: "Utils2 from project A"
                """);

        // Setup Project B
        var rootB = createFile("project-b/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: utilsFlow
                """);

        var utilsB = createFile("project-b/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "Utils from project B"
                  utilsFlow2:
                    - log: "Utils2 from project B"
                """);

        configureFromExistingFile(utilsA);

        renameFlow("utilsFlow", "renamedFlow");

        myFixture.checkResult("""
                flows:
                  renamedFlow:
                    - log: "Utils from project A"
                  utilsFlow2:
                    - log: "Utils2 from project A"
                """);

        assertFileText(rootA,
                """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: renamedFlow
                """);

        // project B unchanged
        assertFileText(rootB,
                """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: utilsFlow
                """);

        assertFileText(utilsB,
                """
                flows:
                  utilsFlow:
                    - log: "Utils from project B"
                  utilsFlow2:
                    - log: "Utils2 from project B"
                """);
    }

    @Test
    void renameInMultiFiles2() {
        // Setup Project A
        var rootA = createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: utilsFlow
                  utilsFlow:
                     - log: "WOW"
                """);

        var utilsA = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "Utils from project A"
                  utilsFlow2:
                    - log: "Utils2 from project A"
                """);

        configureFromExistingFile(utilsA);

        renameFlow("utilsFlow", "renamedFlow");

        myFixture.checkResult("""
                flows:
                  renamedFlow:
                    - log: "Utils from project A"
                  utilsFlow2:
                    - log: "Utils2 from project A"
                """);

        assertFileText(rootA,
                """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: renamedFlow
                  renamedFlow:
                     - log: "WOW"
                """);
    }

    private void assertFileText(PsiFile file, String expected) {
        ReadAction.run(() -> {
            var doc = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
            Assertions.assertNotNull(doc);

            Assertions.assertEquals(expected, doc.getText());
        });
    }

    private void renameFlow(String name, String newName) {
        EdtTestUtil.runInEdtAndWait(() ->
                myFixture.renameElement(key("/flows/" + name).asKeyValue(), newName)
        );
    }
}
