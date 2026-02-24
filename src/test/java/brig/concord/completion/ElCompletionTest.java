// SPDX-License-Identifier: Apache-2.0
package brig.concord.completion;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.psi.BuiltInFunction;
import brig.concord.psi.BuiltInFunctions;
import brig.concord.schema.BuiltInVarsSchema;
import brig.concord.schema.TaskSchemaRegistry;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ElCompletionTest extends ConcordYamlTestBaseJunit5 {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();

        var registry = TaskSchemaRegistry.getInstance(getProject());
        registry.setProvider(taskName -> {
            var path = "/taskSchema/" + taskName + ".schema.json";
            return ElCompletionTest.class.getResourceAsStream(path);
        });
    }

    @Test
    void testBuiltInVarsInExpression() {
        configureFromText("""
                flows:
                  main:
                    - log: "${<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);

        var builtInNames = BuiltInVarsSchema.getInstance().getBuiltInVars().properties().keySet();
        assertThat(lookups).containsAll(builtInNames);
    }

    @Test
    void testSetStepVariablesVisible() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        myVar: "hello"
                        count: 42
                    - log: "${<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("myVar", "count");
    }

    @Test
    void testFlowDocParamsVisible() {
        configureFromText("""
                flows:
                  ##
                  # Process files
                  # in:
                  #   bucket: string, mandatory, S3 bucket
                  #   prefix: string, optional, Filter
                  ##
                  myFlow:
                    - log: "${<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("bucket", "prefix");
    }

    @Test
    void testNoCompletionAfterDotOnScalar() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        myObj: "val"
                    - log: "${myObj.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        // null means zero results or single-match auto-completed; either way no completions
        if (lookups != null) {
            assertThat(lookups).doesNotContain("txId", "myObj");
        }
    }

    @Test
    void testArgumentsVisible() {
        configureFromText("""
                configuration:
                  arguments:
                    appName: "test"
                flows:
                  main:
                    - log: "${<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("appName");
    }

    @Test
    void testTaskOutVariablesVisible() {
        configureFromText("""
                flows:
                  main:
                    - task: myTask
                      out: result
                    - log: "${<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("result");
    }

    @Test
    void testPlainTextExpression() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        x: 1
                    - if: ${<caret>}
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("x");
    }

    // --- Nested property completion tests ---

    @Test
    void testBuiltInObjectProperties() {
        configureFromText("""
                flows:
                  main:
                    - log: "${initiator.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("username", "displayName", "email", "groups", "attributes");
    }

    @Test
    void testNestedBuiltInProperties() {
        configureFromText("""
                flows:
                  main:
                    - log: "${projectInfo.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("orgId", "orgName", "projectId", "projectName",
                "repoId", "repoName", "repoUrl", "repoBranch");
    }

    @Test
    void testSetStepObjectProperties() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        config:
                          host: "localhost"
                          port: 8080
                    - log: "${config.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("host", "port");
    }

    @Test
    void testDeepNestedProperties() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        config:
                          db:
                            host: "localhost"
                            port: 5432
                    - log: "${config.db.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("host", "port");
    }

    @Test
    void testNoPropertiesOnScalar() {
        configureFromText("""
                flows:
                  main:
                    - log: "${txId.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).isEmpty();
        }
    }

    @Test
    void testNoPropertiesOnUnknownVar() {
        configureFromText("""
                flows:
                  main:
                    - log: "${unknown.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).isEmpty();
        }
    }

    @Test
    void testTaskResultProperties() {
        configureFromText("""
                flows:
                  main:
                    - task: strictTask
                      out:
                        k1: "${result.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("ok", "body");
    }

    @Test
    void testBracketSuffixBreaksChain() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        items:
                          - one
                          - two
                    - log: "${items[0].<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).isEmpty();
        }
    }

    @Test
    void testMethodCallBreaksChain() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        config:
                          host: "localhost"
                    - log: "${config.toString().<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).isEmpty();
        }
    }

    @Test
    void testNoVariableCompletionsAfterDot() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        config:
                          host: "localhost"
                    - log: "${config.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        // Should have property completions but NOT variable names
        assertThat(lookups).contains("host");
        assertThat(lookups).doesNotContain("txId", "config", "initiator");
    }

    // --- Built-in function completion tests ---

    @Test
    void testBuiltInFunctionsInExpression() {
        configureFromText("""
                flows:
                  main:
                    - log: "${<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);

        var functionNames = BuiltInFunctions.getInstance().getAll().stream()
                .map(BuiltInFunction::name)
                .toList();
        assertThat(lookups).containsAll(functionNames);
    }

    @Test
    void testNoFunctionCompletionAfterDot() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        config:
                          host: "localhost"
                    - log: "${config.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).doesNotContain("hasVariable", "allVariables", "uuid", "throw");
    }

    @Test
    void testFunctionCompletionInPlainText() {
        configureFromText("""
                flows:
                  main:
                    - if: ${<caret>}
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("hasVariable", "allVariables", "uuid");
    }

    // --- Function insert handler tests ---

    @Test
    void testFunctionWithParamsInsertHandler() {
        configureFromText("""
                flows:
                  main:
                    - if: ${hasVariable<caret>}
                """);

        selectCompletion("hasVariable");

        myFixture.checkResult("""
                flows:
                  main:
                    - if: ${hasVariable(<caret>)}
                """);
    }

    @Test
    void testFunctionNoParamsInsertHandler() {
        configureFromText("""
                flows:
                  main:
                    - if: ${uuid<caret>}
                """);

        selectCompletion("uuid");

        myFixture.checkResult("""
                flows:
                  main:
                    - if: ${uuid()<caret>}
                """);
    }

    private void selectCompletion(String itemText) {
        var items = myFixture.complete(CompletionType.BASIC);
        if (items != null) {
            for (var item : items) {
                if (item.getLookupString().equals(itemText)) {
                    myFixture.getLookup().setCurrentItem(item);
                    break;
                }
            }
            myFixture.type('\n');
        }
    }
}
