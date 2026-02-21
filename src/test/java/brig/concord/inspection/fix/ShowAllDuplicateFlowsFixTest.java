// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.inspection.fix;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.inspection.DuplicateFlowNameInspection;
import com.intellij.openapi.application.ReadAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ShowAllDuplicateFlowsFixTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void showAllDuplicates() {
        myFixture.enableInspections(new DuplicateFlowNameInspection());

        var root = createFile("project-a/concord.yaml", """
            flows:
              main:
                - call: utilsFlow
            """);

        var f1 = createFile("project-a/concord/a.concord.yaml", """
            flows:
              utilsFlow:
                - log: "A"
            """);

        var f2 = createFile("project-a/concord/b.concord.yaml", """
            flows:
              utilsFlow:
                - log: "B"
            """);

        openFileInEditor(f1);

        moveCaretTo(key("/flows/utilsFlow"));

        myFixture.doHighlighting();

        var intention = myFixture.findSingleIntention("Show all duplicates");
        Assertions.assertNotNull(intention);

        Assertions.assertDoesNotThrow(() -> {
            myFixture.getIntentionPreviewText(intention);
            ReadAction.run(() -> intention.invoke(getProject(), myFixture.getEditor(), myFixture.getFile()));
        });
    }

}
