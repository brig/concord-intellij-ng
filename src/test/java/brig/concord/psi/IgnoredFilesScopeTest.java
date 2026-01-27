package brig.concord.psi;

import brig.concord.ConcordYamlTestBase;
import com.intellij.codeInsight.completion.CompletionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IgnoredFilesScopeTest extends ConcordYamlTestBase {

    @Test
    public void testIgnoredRootIsNotDiscovered() {
        var ignoredRoot = createFile(
                "ignored/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """
        );

        var visibleRoot = createFile(
                "visible/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """
        );

        var service = ConcordScopeService.getInstance(getProject());

        service.setIgnoredFileChecker(file -> file.getPath().contains("/ignored/"));

        var roots = service.findRoots();

        Assertions.assertEquals(1, roots.size(), "Should only find one root");
        Assertions.assertEquals(visibleRoot.getVirtualFile(), roots.getFirst().getRootFile(), "Should find the visible root");
    }

    @Test
    public void testIgnoredFileIsNotOutOfScope() {
        var ignoredFile = createFile(
                "ignored/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """
        );

        var service = ConcordScopeService.getInstance(getProject());

        service.setIgnoredFileChecker(file -> file.getPath().contains("/ignored/"));

        // It is ignored, so it should NOT be considered out of scope (no warning)
        // because we don't want to show errors for ignored files.
        boolean outOfScope = service.isOutOfScope(ignoredFile.getVirtualFile());
        Assertions.assertFalse(outOfScope, "Ignored file should not be reported as out of scope");
    }

    @Test
    public void testIgnoredFileInResources() {
        // Root includes everything
        createFile(
                "concord.yaml",
                """
                        resources:
                          concord:
                            - "glob:**/*.concord.yaml"
                        """
        );

        var ignoredFile = createFile(
                "ignored/utils.concord.yaml",
                "flows: {}"
        );

        var visibleFile = createFile(
                "visible/utils.concord.yaml",
                "flows: {}"
        );

        var service = ConcordScopeService.getInstance(getProject());

        service.setIgnoredFileChecker(file -> file.getPath().contains("/ignored/"));

        var scopes = service.getScopesForFile(visibleFile.getVirtualFile());
        Assertions.assertEquals(1, scopes.size());

        var rootScopes = service.findRoots();
        Assertions.assertEquals(1, rootScopes.size());

        var ignoredFileScopes = service.getScopesForFile(ignoredFile.getVirtualFile());
        Assertions.assertTrue(ignoredFileScopes.isEmpty(), "Ignored file should not be returned in getScopesForFile");
    }

    @Test
    public void testCompletionMultiScope() {
        var fa = createFile("concord.yaml",
                """
                flows:
                  default:
                    - call: <caret>
                  myflow1:
                    - log: "ME"
                """);

        createFile(
                "concord/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        flows:
                          flowB:
                            - log: "B"
                        """);

        createFile(
                "concord/ignored/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        flows:
                          flowC:
                            - log: "C"
                        """);

        var service = ConcordScopeService.getInstance(getProject());
        service.setIgnoredFileChecker(file -> file.getPath().contains("/ignored/"));

        myFixture.configureFromExistingVirtualFile(fa.getVirtualFile());

        myFixture.complete(CompletionType.BASIC);

        var lookupElementStrings = myFixture.getLookupElementStrings();
        assertNotNull(lookupElementStrings);
        assertSameElements(lookupElementStrings, "flowB", "myflow1");
    }

    @Test
    public void testInspectionsAreSkippedForIgnoredFiles() {
        var ignoredFile = myFixture.addFileToProject(
                "ignored/bad.concord.yaml",
                """
                        flows:
                          main:
                            - unknownKey: "oops"
                            - log: "Hello"
                            - log: "Duplicate"
                            - log: "Duplicate"
                        """
        );

        var service = ConcordScopeService.getInstance(getProject());
        service.setIgnoredFileChecker(file -> file.getPath().contains("/ignored/"));

        myFixture.configureFromExistingVirtualFile(ignoredFile.getVirtualFile());

        // Run inspections
        var inspections = new com.intellij.codeInspection.LocalInspectionTool[] {
                new brig.concord.inspection.UnknownKeysInspection(),
                new brig.concord.inspection.DuplicatedKeysInspection()
        };

        myFixture.enableInspections(inspections);
        var highlights = myFixture.doHighlighting();

        // Should have no errors because it's ignored
        for (var highlight : highlights) {
            if (highlight.getSeverity().compareTo(com.intellij.lang.annotation.HighlightSeverity.WARNING) >= 0) {
                Assertions.fail("Found unexpected warning/error in ignored file: " + highlight.getDescription());
            }
        }
    }
}
