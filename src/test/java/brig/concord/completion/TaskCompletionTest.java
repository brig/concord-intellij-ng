package brig.concord.completion;

import brig.concord.ConcordYamlTestBase;
import brig.concord.dependency.TaskRegistry;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.Test;

import java.util.Set;

public class TaskCompletionTest extends ConcordYamlTestBase {

    @Test
    public void testTaskCompletion() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: <caret>
                """);

        // Set up task names for this scope
        TaskRegistry.getInstance(getProject()).setTaskNames(file.getVirtualFile(), Set.of("http", "slack", "git"));

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertContainsElements(lookupElementStrings, "http", "slack", "git");
    }

    @Test
    public void testTaskCompletionWithPrefix() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: ht<caret>
                """);

        TaskRegistry.getInstance(getProject()).setTaskNames(file.getVirtualFile(), Set.of("http", "httpPost", "slack"));

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertContainsElements(lookupElementStrings, "http", "httpPost");
        assertDoesntContain(lookupElementStrings, "slack");
    }

    @Test
    public void testTaskCompletionEmpty() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: <caret>
                """);

        // No task names registered
        TaskRegistry.getInstance(getProject()).setTaskNames(file.getVirtualFile(), Set.of());

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        // Should be null or empty when no completions available
        assertTrue(lookupElementStrings == null || lookupElementStrings.isEmpty());
    }

    @Test
    public void testTaskCompletionInLoop() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: <caret>
                      in:
                        url: "http://example.com"
                      loop:
                        items:
                          - a
                          - b
                """);

        TaskRegistry.getInstance(getProject()).setTaskNames(file.getVirtualFile(), Set.of("http", "log"));

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertContainsElements(lookupElementStrings, "http", "log");
    }

    @Test
    public void testTaskCompletionMultiScope() {
        // Scope A with its tasks
        var fileA = createFile("project-a/concord.yaml", """
                flows:
                  main:
                    - task: <caret>
                """);

        // Scope B with different tasks
        var fileB = createFile("project-b/concord.yaml", """
                flows:
                  main:
                    - log: "hello"
                """);

        var registry = TaskRegistry.getInstance(getProject());
        registry.setTaskNames(fileA.getVirtualFile(), Set.of("http", "slack"));
        registry.setTaskNames(fileB.getVirtualFile(), Set.of("unexpected1"));

        myFixture.configureFromExistingVirtualFile(fileA.getVirtualFile());
        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertContainsElements(lookupElementStrings, "http", "slack");
        assertDoesntContain(lookupElementStrings, "unexpected1");
    }
}
