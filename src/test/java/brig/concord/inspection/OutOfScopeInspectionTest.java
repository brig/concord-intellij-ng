package brig.concord.inspection;

import brig.concord.assertions.InspectionAssertions;
import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

class OutOfScopeInspectionTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(OutOfScopeInspection.class);
    }

    @Test
    void testRootFile_noWarning() {
        // Root files (concord.yaml) define their own scope, so they should never get this warning
        configureFromText("""
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - log: "Hello"
                """);

        assertNoWarnings();
    }

    @Test
    void testFileInScope_noWarning() {
        // Create root that defines the scope
        createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                """);

        // Create a file that matches the default concord/** pattern
        var utilsFile = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "Utils"
                """);

        configureFromExistingFile(utilsFile);

        assertNoWarnings();
    }

    @Test
    void testFileOutOfScope_warning() {
        // Create root with restricted scope
        createFile("project-a/concord.yaml", """
                resources:
                  concord:
                    - "glob:flows/*.concord.yaml"
                configuration:
                  runtime: concord-v2
                """);

        // Create a file outside the scope pattern
        var orphanFile = createFile("project-a/other/orphan.concord.yaml", """
                flows:
                  orphanFlow:
                    - log: "Orphan"
                """);

        configureFromExistingFile(orphanFile);

        // Should have a warning on the document
        InspectionAssertions.assertHasOutOfScopeWarning(myFixture);
    }

    @Test
    void testFileMatchingCustomPattern_noWarning() {
        // Create root with custom scope pattern
        createFile("project-a/concord.yaml", """
                resources:
                  concord:
                    - "glob:custom/**/*.concord.yaml"
                configuration:
                  runtime: concord-v2
                """);

        // Create a file that matches the custom pattern
        var customFile = createFile("project-a/custom/nested/my.concord.yaml", """
                flows:
                  myFlow:
                    - log: "Custom"
                """);

        configureFromExistingFile(customFile);

        assertNoWarnings();
    }

    @Test
    void testFileInMultipleScopes_noWarning() {
        // Create two roots whose scopes overlap
        createFile("project-a/concord.yaml", """
                resources:
                  concord:
                    - "glob:**/*.concord.yaml"
                configuration:
                  runtime: concord-v2
                """);

        createFile("project-a/subproject/concord.yaml", """
                resources:
                  concord:
                    - "glob:**/*.concord.yaml"
                configuration:
                  runtime: concord-v2
                """);

        // Create a file that could be in multiple scopes
        var sharedFile = createFile("project-a/subproject/shared.concord.yaml", """
                flows:
                  sharedFlow:
                    - log: "Shared"
                """);

        configureFromExistingFile(sharedFile);

        assertNoWarnings();
    }

    @Test
    void testDotConcordYamlRoot_noWarning() {
        // Test with .concord.yaml root file name variant
        configureFromText(".concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - log: "Hello"
                """);

        assertNoWarnings();
    }

    @Test
    void testEmptyFileOutOfScope_noException() {
        // Create root with restricted scope
        createFile("project-a/concord.yaml", """
                resources:
                  concord:
                    - "glob:flows/*.concord.yaml"
                configuration:
                  runtime: concord-v2
                """);

        // Create an empty file outside the scope pattern
        var emptyFile = createFile("project-a/other/empty.concord.yaml", "");

        configureFromExistingFile(emptyFile);

        // Should not throw exception on empty document, just no warnings
        assertNoWarnings();
    }
}
