package brig.concord.completion;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.dependency.TaskRegistry;
import com.intellij.codeInsight.completion.CompletionType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TaskCompletionTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testTaskCompletion() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: <caret>
                """);

        // Set up task names for this scope
        TaskRegistry.getInstance(getProject()).setTaskNames(file.getVirtualFile(), Set.of("http", "slack", "git"));

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).contains("http", "slack", "git");
    }

    @Test
    void testTaskCompletionWithPrefix() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: ht<caret>
                """);

        TaskRegistry.getInstance(getProject()).setTaskNames(file.getVirtualFile(), Set.of("http", "httpPost", "slack"));

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).contains("http", "httpPost");
        assertThat(lookupElementStrings).doesNotContain("slack");
    }

    @Test
    void testTaskCompletionEmpty() {
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
        Assertions.assertTrue(lookupElementStrings == null || lookupElementStrings.isEmpty());
    }

    @Test
    void testTaskCompletionInLoop() {
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
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).contains("http", "log");
    }

    @Test
    void testTaskCompletionMultiScope() {
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
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).contains("http", "slack");
        assertThat(lookupElementStrings).doesNotContain("unexpected1");
    }

    @Test
    void testTaskOutWithSchemaCompletionForScalarResult() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: concord
                      out: <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).isEmpty();
    }

    @Test
    void testTaskOutWithSchemaCompletionForObjectResult() {
        var file = configureFromText("""
                flows:
                  main:
                    - task: concord
                      out:
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElementStrings);
        assertThat(lookupElementStrings).isEmpty();
    }
}
