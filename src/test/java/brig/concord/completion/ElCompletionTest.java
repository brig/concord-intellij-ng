package brig.concord.completion;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.psi.ConcordBuiltInVars;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ElCompletionTest extends ConcordYamlTestBaseJunit5 {

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

        var builtInNames = ConcordBuiltInVars.VARS.stream()
                .map(ConcordBuiltInVars.BuiltInVar::name)
                .toList();
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
    void testSetStepObjectProperties() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        obj:
                          a: 1
                          b: 2
                    - log: "${obj.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("a", "b");
    }

    @Test
    void testSetStepNestedProperties() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        obj:
                          inner:
                            x: 1
                            y: 2
                    - log: "${obj.inner.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("x", "y");
    }

    @Test
    void testArgumentsObjectProperties() {
        configureFromText("""
                configuration:
                  arguments:
                    config:
                      url: "http://example.com"
                      timeout: 10
                flows:
                  main:
                    - log: "${config.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("url", "timeout");
    }

    @Test
    void testNoPropertiesForScalarSet() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        x: "hello"
                    - log: "${x.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).isEmpty();
        }
    }

    @Test
    void testNoCompletionAfterDot() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        myObj: "val"
                    - log: "${myObj.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        // null means zero results or single-match auto-completed; either way our contributor didn't fire
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
    void testTaskOutSchemaProperties() {
        configureFromText("""
                flows:
                  main:
                    - task: concord
                      out: concordResult
                    - log: "${concordResult.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("ok", "id", "ids");
    }

    @Test
    void testCallOutMappingProperties() {
        configureFromText("""
                flows:
                  main:
                    - call: inner
                      out:
                        key:
                          key2: value
                    - log: "${key.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("key2");
    }

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
        assertThat(lookups).contains("displayName", "email", "username", "groups", "attributes");
    }

    @Test
    void testBuiltInProjectInfoProperties() {
        configureFromText("""
                flows:
                  main:
                    - log: "${projectInfo.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("orgName", "projectName", "repoUrl");
    }

    @Test
    void testBuiltInScalarNoProperties() {
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

    @Test
    void testTransitiveBuiltInProperties() {
        configureFromText("""
                configuration:
                  arguments:
                    myUser: "${initiator}"
                flows:
                  main:
                    - log: "${myUser.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("displayName", "email");
    }

    @Test
    void testTransitiveSetStepProperties() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        obj:
                          a: 1
                          b: 2
                    - set:
                        ref: "${obj}"
                    - log: "${ref.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("a", "b");
    }

    @Test
    void testTransitiveWithChain() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        obj:
                          inner:
                            a: 1
                    - set:
                        x: "${obj.inner}"
                    - log: "${x.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        assertNotNull(lookups);
        assertThat(lookups).contains("a");
    }

    @Test
    void testTransitiveScalarNoProperties() {
        configureFromText("""
                configuration:
                  arguments:
                    name: "${txId}"
                flows:
                  main:
                    - log: "${name.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).isEmpty();
        }
    }

    @Test
    void testNonElScalarNoProperties() {
        configureFromText("""
                configuration:
                  arguments:
                    name: "hello"
                flows:
                  main:
                    - log: "${name.<caret>}"
                """);

        myFixture.complete(CompletionType.BASIC);

        var lookups = myFixture.getLookupElementStrings();
        if (lookups != null) {
            assertThat(lookups).isEmpty();
        }
    }
}
