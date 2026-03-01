// SPDX-License-Identifier: Apache-2.0
package brig.concord.completion;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.dependency.TaskInfo;
import brig.concord.dependency.TaskMethod;
import brig.concord.dependency.TaskRegistry;
import brig.concord.psi.BuiltInFunction;
import brig.concord.psi.BuiltInFunctions;
import brig.concord.schema.BuiltInVarsSchema;
import brig.concord.schema.SchemaType;
import brig.concord.schema.TaskSchemaRegistry;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    void testCallOutVariablesFromFlowDoc() {
        configureFromText("""
                flows:
                  default:
                    - call: inner
                      out:
                        myOut: "${<caret>}"

                  ##
                  # in:
                  #   inputParam: string, mandatory, some input
                  # out:
                  #   outFromInner: string, required
                  ##
                  inner:
                    - set:
                        outFromInner: "good"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("outFromInner");
        assertThat(lookups).doesNotContain("inputParam");
    }

    @Test
    void testCallOutVariablesNoFlowDoc() {
        configureFromText("""
                flows:
                  default:
                    - call: inner
                      out:
                        myOut: "${<caret>}"

                  inner:
                    - set:
                        outFromInner: "good"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).doesNotContain("outFromInner");
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

        selectUniqueCompletion();

        myFixture.checkResult("""
                flows:
                  main:
                    - if: ${uuid()<caret>}
                """);
    }

    // --- Task name EL completion tests ---

    @Test
    void testTaskNamesInExpression() {
        var file = configureFromText("""
                flows:
                  main:
                    - log: "${<caret>}"
                """);

        var taskInfos = createTestTaskInfos();
        TaskRegistry.getInstance(getProject()).setTaskInfos(file.getVirtualFile(), taskInfos);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("datetime", "slack");
    }

    @Test
    void testTaskMethodsAfterDot() {
        var file = configureFromText("""
                flows:
                  main:
                    - log: "${datetime.<caret>}"
                """);

        var taskInfos = createTestTaskInfos();
        TaskRegistry.getInstance(getProject()).setTaskInfos(file.getVirtualFile(), taskInfos);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("current()", "format(any, string)");
    }

    @Test
    void testNoTaskMethodsForUnknownTask() {
        var file = configureFromText("""
                flows:
                  main:
                    - log: "${unknown.<caret>}"
                """);

        var taskInfos = createTestTaskInfos();
        TaskRegistry.getInstance(getProject()).setTaskInfos(file.getVirtualFile(), taskInfos);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).doesNotContain("current()", "format(any, string)");
        }
    }

    @Test
    void testTaskMethodNoParamsInsertHandler() {
        var file = configureFromText("""
                flows:
                  main:
                    - if: ${datetime.<caret>}
                """);

        var taskInfos = createTestTaskInfos();
        TaskRegistry.getInstance(getProject()).setTaskInfos(file.getVirtualFile(), taskInfos);

        selectCompletion("current()");

        myFixture.checkResult("""
                flows:
                  main:
                    - if: ${datetime.current()<caret>}
                """);
    }

    @Test
    void testTaskMethodWithParamsInsertHandler() {
        var file = configureFromText("""
                flows:
                  main:
                    - if: ${datetime.<caret>}
                """);

        var taskInfos = createTestTaskInfos();
        TaskRegistry.getInstance(getProject()).setTaskInfos(file.getVirtualFile(), taskInfos);

        selectCompletion("format(any, string)");

        myFixture.checkResult("""
                flows:
                  main:
                    - if: ${datetime.format(<caret>)}
                """);
    }

    @Test
    void testTaskNamesNotAfterDot() {
        var file = configureFromText("""
                flows:
                  main:
                    - set:
                        config:
                          host: "localhost"
                    - log: "${config.<caret>}"
                """);

        var taskInfos = createTestTaskInfos();
        TaskRegistry.getInstance(getProject()).setTaskInfos(file.getVirtualFile(), taskInfos);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("host");
        assertThat(lookups).doesNotContain("datetime", "slack");
    }

    private static Map<String, TaskInfo> createTestTaskInfos() {
        Map<String, TaskInfo> infos = new LinkedHashMap<>();

        var datetimeMethods = List.of(
                new TaskMethod("current", SchemaType.Scalar.STRING, List.of()),
                new TaskMethod("format", SchemaType.Scalar.STRING, List.of(SchemaType.ANY, SchemaType.Scalar.STRING))
        );
        infos.put("datetime", new TaskInfo("datetime", datetimeMethods));

        var slackMethods = List.of(
                new TaskMethod("sendMessage", SchemaType.ANY, List.of(SchemaType.Scalar.STRING, SchemaType.Scalar.STRING))
        );
        infos.put("slack", new TaskInfo("slack", slackMethods));

        return infos;
    }

    private void selectCompletion(String itemText) {
        var items = myFixture.complete(CompletionType.BASIC);
        assertNotNull(items, "Expected multiple completion items, but got single auto-insert");
        assertThat(items).as("No completion items returned").isNotEmpty();

        var lookup = myFixture.getLookup();
        assertNotNull(lookup, "Lookup is not showing");

        boolean found = false;
        for (var item : items) {
            if (item.getLookupString().equals(itemText)) {
                lookup.setCurrentItem(item);
                found = true;
                break;
            }
        }
        assertThat(found).as("Completion item '%s' not found in: %s", itemText, myFixture.getLookupElementStrings()).isTrue();

        myFixture.type('\n');
    }

    private void selectUniqueCompletion() {
        var items = myFixture.complete(CompletionType.BASIC);
        assertNull(items, "Expected single auto-insert, but got multiple items: " + myFixture.getLookupElementStrings());
    }
}
