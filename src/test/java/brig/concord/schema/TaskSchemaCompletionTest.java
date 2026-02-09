package brig.concord.schema;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TaskSchemaCompletionTest extends ConcordYamlTestBaseJunit5 {

    @Test
    public void testInParamsCompletion() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElements = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElements);
        // Should have the discriminator key "action" in the list
        assertContainsElements(lookupElements, "action");
    }

    @Test
    public void testActionEnumCompletion() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElements = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElements);
        assertContainsElements(lookupElements, "start", "startExternal", "fork", "kill", "createApiKey", "createOrUpdateApiKey");
    }

    @Test
    public void testStartActionParams() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: start
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElements = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElements);
        assertContainsElements(lookupElements, "project", "payload", "entryPoint", "sync", "activeProfiles");
    }

    @Test
    public void testKillActionParams() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: kill
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElements = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElements);
        assertContainsElements(lookupElements, "instanceId", "sync");
        // Should not have start-specific params
        assertDoesntContain(lookupElements, "project", "payload", "entryPoint");
    }

    @Test
    public void testCreateApiKeyParams() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: createApiKey
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElements = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElements);
        assertContainsElements(lookupElements, "userId", "username", "userType", "name");
    }

    @Test
    public void testOutParamsCompletion() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: start
                        project: myProject
                      out:
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElements = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElements);
        assertContainsElements(lookupElements, "ok", "id", "ids", "name", "key", "result");
    }

    @Test
    public void testUnknownTaskFallsBackToAnyMap() {
        configureFromText("""
                flows:
                  main:
                    - task: unknownTask
                      in:
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        // Should not crash; falls back to any map (no specific completions)
        var lookupElements = myFixture.getLookupElementStrings();
        // No schema for unknownTask, so no specific completions
        Assertions.assertTrue(lookupElements == null || lookupElements.isEmpty());
    }

    @Test
    public void testDiscriminatorKeyFirst() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElements = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElements);
        Assertions.assertFalse(lookupElements.isEmpty());
        // "action" should be among the first completions (discriminator key)
        Assertions.assertEquals("action", lookupElements.get(0));
    }

    @Test
    public void testForkActionParams() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: fork
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElements = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElements);
        assertContainsElements(lookupElements, "forks", "entryPoint", "sync");
        // Should not have kill-specific params
        assertDoesntContain(lookupElements, "instanceId");
    }

    @Test
    public void testStartExternalParams() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      in:
                        action: startExternal
                        <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElements = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElements);
        // Should have start params + external-specific
        assertContainsElements(lookupElements, "project", "baseUrl", "apiKey");
    }
}
