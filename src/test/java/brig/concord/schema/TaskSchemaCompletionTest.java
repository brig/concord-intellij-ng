package brig.concord.schema;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskSchemaCompletionTest extends ConcordYamlTestBaseJunit5 {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();

        var registry = TaskSchemaRegistry.getInstance(getProject());
        registry.setProvider(taskName -> {
            var path = "/taskSchema/" + taskName + ".schema.json";
            return TaskSchemaCompletionTest.class.getResourceAsStream(path);
        });
    }

    @Test
    void testInParamsCompletion() {
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
        assertThat(lookupElements).contains("action");
    }

    @Test
    void testActionEnumCompletion() {
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
        assertThat(lookupElements).contains("start", "startExternal", "fork", "kill", "createApiKey", "createOrUpdateApiKey");
    }

    @Test
    void testStartActionParams() {
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
        assertThat(lookupElements).contains("project", "payload", "entryPoint", "sync", "activeProfiles");
    }

    @Test
    void testKillActionParams() {
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
        assertThat(lookupElements).contains("instanceId", "sync");
        // Should not have start-specific params
        assertThat(lookupElements).doesNotContain("project", "payload", "entryPoint");
    }

    @Test
    void testCreateApiKeyParams() {
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
        assertThat(lookupElements).contains("userId", "username", "userType", "name");
    }

    @Test
    void testOutParamsCompletion() {
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
        assertThat(lookupElements).isEmpty();
    }

    @Test
    void testUnknownTaskFallsBackToAnyMap() {
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
    void testDiscriminatorKeyFirst() {
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
    void testForkActionParams() {
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
        assertThat(lookupElements).contains("forks", "entryPoint", "sync");
        // Should not have kill-specific params
        assertThat(lookupElements).doesNotContain("instanceId");
    }

    @Test
    void testStartExternalParams() {
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
        assertThat(lookupElements).contains("project", "baseUrl", "apiKey");
    }

    // --- nested object completion tests ---

    @Test
    void testNestedObjectCompletion() {
        configureFromText("""
                flows:
                  main:
                    - task: nestedObject
                      in:
                        url: "http://example.com"
                        auth:
                          <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElements = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElements);
        assertThat(lookupElements).contains("basic", "token");
    }

    @Test
    void testDeepNestedObjectCompletion() {
        configureFromText("""
                flows:
                  main:
                    - task: nestedObject
                      in:
                        url: "http://example.com"
                        auth:
                          basic:
                            <caret>
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookupElements = myFixture.getLookupElementStrings();
        Assertions.assertNotNull(lookupElements);
        assertThat(lookupElements).contains("username", "password");
    }
}
