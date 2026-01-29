package brig.concord.completion;

import brig.concord.ConcordYamlTestBase;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.Test;

public class CompletionTest extends ConcordYamlTestBase {

    @Test
    public void testCompletion() {
        configureFromResource("/completion/00.concord.yaml");

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "p1");
    }

    @Test
    public void testCompletionSteps() {
        configureFromResource("/completion/01.concord.yaml");

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "block", "call", "checkpoint", "exit", "expr", "form", "if", "log", "logYaml", "parallel", "return", "script", "set", "suspend", "switch", "task", "throw", "try");
    }

    @Test
    public void testCompletionSimpleFlowNames() {
        configureFromResource("/completion/02.concord.yaml");

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "myflow1");
    }

    @Test
    public void testCompletionMultiScope() {
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
        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "myflow1");
    }

    @Test
    public void testCompletionTriggerEntryPoint() {
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
        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "myFlow");
    }
}
