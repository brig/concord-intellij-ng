package brig.concord.completion;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.testFramework.EdtTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CompletionTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testCompletion() {
        configureFromResource("/completion/00.concord.yaml");

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).containsExactlyInAnyOrder( "p1");
    }

    @Test
    void testCompletionSteps() {
        configureFromResource("/completion/01.concord.yaml");

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).containsExactlyInAnyOrder( "block", "call", "checkpoint", "exit", "expr", "form", "if", "log", "logYaml", "parallel", "return", "script", "set", "suspend", "switch", "task", "throw", "try");
    }

    @Test
    void testCompletionSimpleFlowNames() {
        configureFromResource("/completion/02.concord.yaml");

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).containsExactlyInAnyOrder( "myflow1");
    }

    @Test
    void testCompletionMultiScope() {
        var fa = createFile("project-a/concord.yaml",
                """
                        flows:
                          default:
                            - call: <caret>
                          myflow1:
                            - log: "ME"
                        """);

        createFile(
                "project-b/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        flows:
                          flowB:
                            - log: "B"
                        """);

        createFile(
                "project-c/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        flows:
                          flowC:
                            - log: "C"
                        """);

        myFixture.configureFromExistingVirtualFile(fa.getVirtualFile());

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).containsExactlyInAnyOrder( "myflow1");
    }

    @Test
    void testCompletionTriggerEntryPoint() {
        configureFromText(
                """
                        flows:
                          myFlow:
                            - log: "default"

                        triggers:
                          - manual:
                              entryPoint: <caret>
                        """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).containsExactlyInAnyOrder( "myFlow");
    }

    @Test
    void testCompletionMultiScopeAfterEdit() {
        var fa = createFile("project-a/concord.yaml",
                """
                        flows:
                          default:
                            - call: <caret>
                          myflow1:
                            - log: "ME"
                        """);

        var fb = createFile(
                "concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        flows:
                          flowB:
                            - log: "B"
                        """);

        myFixture.configureFromExistingVirtualFile(fa.getVirtualFile());

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).containsExactlyInAnyOrder( "myflow1");

        // Edit flowB -> flowB1
        EdtTestUtil.runInEdtAndWait(() -> {
            myFixture.openFileInEditor(fb.getVirtualFile());

            var document = myFixture.getEditor().getDocument();
            var text = document.getText();
            var offset = text.indexOf("flowB:");

            WriteCommandAction.runWriteCommandAction(getProject(), () ->
                    document.replaceString(offset, offset + 5, "flowB1"));

            PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
        });

        // Verify completion still works after edit
        myFixture.configureFromExistingVirtualFile(fa.getVirtualFile());
        myFixture.complete(CompletionType.BASIC);

        lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).containsExactlyInAnyOrder( "myflow1");
    }
}
