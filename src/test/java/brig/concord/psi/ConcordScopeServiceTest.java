package brig.concord.psi;

import brig.concord.ConcordYamlTestBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.testFramework.EdtTestUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class ConcordScopeServiceTest extends ConcordYamlTestBase {

    @Test
    public void testFindRootsWithSingleRoot() {
        myFixture.addFileToProject(
                "concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        flows:
                          flowA:
                            - log: "A"
                        """
        );

        var service = ConcordScopeService.getInstance(getProject());
        var roots = ReadAction.compute(service::findRoots);

        Assertions.assertNotNull(roots);
        Assertions.assertEquals(1, roots.size());
    }

    @Test
    public void testGetScopesForRootFile() {
        var yaml = myFixture.addFileToProject(
                "concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """);

        var vFile = yaml.getVirtualFile();
        var service = ConcordScopeService.getInstance(getProject());

        var scopes = ReadAction.compute(() -> service.getScopesForFile(vFile));
        Assertions.assertNotNull(scopes);
        Assertions.assertEquals(1, scopes.size());
    }

    @Test
    public void testCreateSearchScopeFromElement() {
        var yaml = myFixture.addFileToProject(
                "concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        flows:
                          main:
                            - log: "Hello"
                        """);

        var service = ConcordScopeService.getInstance(getProject());
        var scope = ReadAction.compute(() -> service.createSearchScope(yaml));

        Assertions.assertNotNull(scope);
    }

    @Test
    public void testCreateSearchScopeFromVirtualFile() {
        var yaml = myFixture.addFileToProject(
                "concord.yaml",
                """
                        configuration:
                            runtime: concord-v2
                        """);

        var vFile = yaml.getVirtualFile();
        var service = ConcordScopeService.getInstance(getProject());

        var scope = ReadAction.compute(() -> service.createSearchScope(vFile));
        Assertions.assertNotNull(scope);
    }

    @Test
    public void testMultipleScopesForFile() {
        // Root 1 at the top, but it ONLY includes files in sub/shared/
        var root1 = myFixture.addFileToProject(
                "concord.yaml",
                """
                        resources:
                          concord:
                            - "glob:sub/shared/*.concord.yaml"
                        """);

        // Root 2 in sub/, includes files in shared/
        var root2 = myFixture.addFileToProject(
                "sub/concord.yaml",
                """
                        resources:
                          concord:
                            - "glob:shared/*.concord.yaml"
                        """);

        // The shared file is in sub/shared/, which is under both root 1 and root 2
        var sharedFile = myFixture.addFileToProject(
                "sub/shared/utils.concord.yaml",
                "flows: {}"
        );

        var service = ConcordScopeService.getInstance(getProject());
        var roots = ReadAction.compute(service::findRoots);
        // Both should be roots because root1 doesn't include root2's file in its patterns
        Assertions.assertEquals(2, roots.size());

        var scopes = ReadAction.compute(() -> service.getScopesForFile(sharedFile.getVirtualFile()));
        Assertions.assertEquals(2, scopes.size());
    }

    @Test
    public void testMultipleRoots() {
        var root1 = myFixture.addFileToProject(
                "project-a/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        flows:
                          flowA:
                            - log: "A"
                        """
        );

        var root2 = myFixture.addFileToProject(
                "project-b/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        flows:
                          flowB:
                            - log: "B"
                        """
        );

        var service = ConcordScopeService.getInstance(getProject());
        var roots = ReadAction.compute(service::findRoots);

        // Should find both roots
        Assertions.assertTrue(roots.size() >= 2, () -> "Expected at least 2 roots, found: " + roots.size());
    }

    @Test
    public void testNestedFileInScope() {
        // Create root with default resources pattern (which includes concord/**/*.concord.yaml)
        var root = myFixture.addFileToProject(
                "my-project/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """
        );

        // Create a nested file in the concord subdirectory (matches default pattern)
        var nestedFile = myFixture.addFileToProject(
                "my-project/concord/utils.concord.yaml",
                """
                        flows:
                          utilFlow:
                            - log: "Util"
                        """
        );

        var service = ConcordScopeService.getInstance(getProject());
        var roots = ReadAction.compute(service::findRoots);
        Assertions.assertEquals(1, roots.size());

        // Find the root for my-project
        var myProjectRoot = roots.getFirst();
        Assertions.assertNotNull(myProjectRoot, "Should find my-project root");

        // The nested file should be contained in the scope
        // Note: This test verifies the pattern matching works with the default concord pattern
        Assertions.assertTrue(ReadAction.compute(() -> myProjectRoot.contains(nestedFile.getVirtualFile())), "Nested file should be contained in scope");
    }

    @Test
    public void testFileOutsideScope() {
        // Create root with pattern
        var root = myFixture.addFileToProject(
                "narrow-project/concord.yaml",
                """
                        resources:
                          concord:
                            - "glob:flows/*.concord.yaml"
                        configuration:
                          runtime: concord-v2
                        """
        );

        // Create a file outside the pattern
        var outsideFile = myFixture.addFileToProject(
                "narrow-project/other/concord.yaml",
                """
                        flows:
                          otherFlow:
                            - log: "Other"
                        """
        );

        var service = ConcordScopeService.getInstance(getProject());
        var roots = ReadAction.compute(service::findRoots);
        Assertions.assertEquals(2, roots.size());

        // Find the root
        var narrowRoot = roots.stream()
                .filter(r -> r.getRootFile().equals(root.getVirtualFile()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(narrowRoot, "Should find narrow-project root");
    }

    /**
     * This test verifies that cache is invalidated when concord.yaml content changes.
     * ConcordModificationTracker tracks content changes of concord.yaml files.
     */
    @Test
    public void testCacheInvalidationOnPatternChange() {
        // Create root with narrow pattern - only includes flows/*.concord.yaml
        var root = myFixture.addFileToProject(
                "my-project/concord.yaml",
                """
                        resources:
                          concord:
                            - "glob:flows/*.concord.yaml"
                        configuration:
                          runtime: concord-v2
                        """);

        // Create file that matches the pattern
        var insideFile = myFixture.addFileToProject(
                "my-project/flows/utils.concord.yaml",
                "flows: {}");

        // Create file outside the pattern (in other/ directory)
        var outsideFile = myFixture.addFileToProject(
                "my-project/other/helper.concord.yaml",
                "flows: {}");

        var service = ConcordScopeService.getInstance(getProject());

        // Verify initial state - outsideFile should NOT be in scope
        var initialScopes = ReadAction.compute(() -> service.getScopesForFile(outsideFile.getVirtualFile()));
        Assertions.assertTrue(initialScopes.isEmpty(),
                "Initially, outsideFile should NOT be in any scope");

        // insideFile SHOULD be in scope
        var insideScopes = ReadAction.compute(() -> service.getScopesForFile(insideFile.getVirtualFile()));
        Assertions.assertEquals(1, insideScopes.size(),
                "insideFile should be in scope");

        // Now change the pattern to include other/ directory
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                var vf = root.getVirtualFile();
                vf.setBinaryContent("""
                        resources:
                          concord:
                            - "glob:flows/*.concord.yaml"
                            - "glob:other/*.concord.yaml"
                        configuration:
                          runtime: concord-v2
                        """.getBytes(StandardCharsets.UTF_8));
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Force PSI to refresh
        EdtTestUtil.runInEdtAndWait(() ->
                PsiDocumentManager.getInstance(getProject()).commitAllDocuments()
        );

        // Verify that file content actually changed (PSI was refreshed)
        var updatedContent = ReadAction.compute(() ->
                PsiManager.getInstance(getProject())
                        .findFile(root.getVirtualFile())
                        .getText()
        );
        Assertions.assertTrue(updatedContent.contains("glob:other/*.concord.yaml"),
                "File content should be updated with new pattern");

        // Now outsideFile SHOULD be in scope after pattern change
        // THIS ASSERTION WILL FAIL due to stale cache!
        var updatedScopes = ReadAction.compute(() -> service.getScopesForFile(outsideFile.getVirtualFile()));
        Assertions.assertEquals(1, updatedScopes.size(),
                "After pattern change, outsideFile SHOULD be in scope (cache invalidation bug!)");
    }

    @Test
    public void testSameServiceInstance() {
        configureFromText("""
                configuration:
                  runtime: concord-v2
                """);

        var service1 = ConcordScopeService.getInstance(getProject());
        var service2 = ConcordScopeService.getInstance(getProject());

        assertSame("Should return same service instance", service1, service2);
    }

    @Test
    public void testCallNavigatesToCorrectScope() {
        // Setup Project A
        var rootA = createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: utilsFlow
                """);

        var utilsA = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "Utils from project A"
                """);

        // Setup Project B
        var rootB = createFile("project-b/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: utilsFlow
                """);

        var utilsB = createFile("project-b/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "Utils from project B"
                """);

        // Verify Project A
        configureFromExistingFile(rootA);
        assertCallResolvesTo("/flows/main[0]/call", utilsA);

        // Verify Project B
        configureFromExistingFile(rootB);
        assertCallResolvesTo("/flows/main[0]/call", utilsB);
    }

    @Test
    public void testCallNavigatesToCorrectScopeLocalDefinition() {
        // Setup Project A (Local definition)
        var rootA = createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: utilsFlow
                  utilsFlow:
                    - log: "Utils from project A"
                """);

        // Setup Project B (Local definition)
        var rootB = createFile("project-b/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: utilsFlow
                  utilsFlow:
                    - log: "Utils from project B"
                """);

        // Verify Project A
        configureFromExistingFile(rootA);
        assertCallResolvesTo("/flows/main[0]/call", rootA);

        // Verify Project B
        configureFromExistingFile(rootB);
        assertCallResolvesTo("/flows/main[0]/call", rootB);
    }

    @Test
    public void testCallNavigatesToCorrectScopeOverwriteLocalDefinition() {
        // Setup Project A
        var rootA = createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: utilsFlow
                  utilsFlow:
                    - log: "Utils from project A (main)"
                """);

        var utilsA = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "Utils from project A"
                """);

        // Setup Project B
        var rootB = createFile("project-b/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: utilsFlow
                  utilsFlow:
                    - log: "Utils from project B (main)"
                """);

        var utilsB = createFile("project-b/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "Utils from project B"
                """);

        // Verify Project A
        configureFromExistingFile(rootA);
        assertCallResolvesTo("/flows/main[0]/call", rootA, utilsA);

        // Verify Project B
        configureFromExistingFile(rootB);
        assertCallResolvesTo("/flows/main[0]/call", rootB, utilsB);
    }

    @Test
    public void testSearchScopeIsolation() {
        // Create two isolated projects
        var rootA = createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                """);

        var utilsA = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  sharedFlowName:
                    - log: "A"
                """);

        var rootB = createFile("project-b/concord.yaml", """
                configuration:
                  runtime: concord-v2
                """);

        var utilsB = createFile("project-b/concord/utils.concord.yaml", """
                flows:
                  sharedFlowName:
                    - log: "B"
                """);

        var service = ConcordScopeService.getInstance(getProject());

        // Get search scope for project-a
        var scopeA = ReadAction.compute(() -> service.createSearchScope(rootA.getVirtualFile()));

        // Verify scope-a contains project-a files
        Assertions.assertTrue(scopeA.contains(rootA.getVirtualFile()), "Scope A should contain rootA");
        Assertions.assertTrue(scopeA.contains(utilsA.getVirtualFile()), "Scope A should contain utilsA");

        // Verify scope-a does NOT contain project-b files
        Assertions.assertFalse(scopeA.contains(rootB.getVirtualFile()), "Scope A should NOT contain rootB");
        Assertions.assertFalse(scopeA.contains(utilsB.getVirtualFile()), "Scope A should NOT contain utilsB");

        // Get search scope for project-b
        var scopeB = ReadAction.compute(() -> service.createSearchScope(rootB.getVirtualFile()));

        // Verify scope-b contains project-b files
        Assertions.assertTrue(scopeB.contains(rootB.getVirtualFile()), "Scope B should contain rootB");
        Assertions.assertTrue(scopeB.contains(utilsB.getVirtualFile()), "Scope B should contain utilsB");

        // Verify scope-b does NOT contain project-a files
        Assertions.assertFalse(scopeB.contains(rootA.getVirtualFile()), "Scope B should NOT contain rootA");
        Assertions.assertFalse(scopeB.contains(utilsA.getVirtualFile()), "Scope B should NOT contain utilsA");
    }

    private void assertCallResolvesTo(String jsonPath, PsiFile... expectedFiles) {
        var callValue = value(jsonPath).element();
        Assertions.assertNotNull(callValue, "Should find call value at " + jsonPath);

        ReadAction.run(() -> {
            var reference = callValue.getReference();
            Assertions.assertNotNull(reference, "Call value should have a reference");
            Assertions.assertInstanceOf(PsiPolyVariantReference.class, reference, "Flow definition reference should implement PsiPolyVariantReference");

            var results = ((PsiPolyVariantReference)reference).multiResolve(false);
            Assertions.assertTrue(results.length > 0, "Reference should resolve");

            var resolvedPaths = new java.util.HashSet<String>();
            for (var r : results) {
                if (r == null) {
                    continue;
                }
                var el = r.getElement();
                if (el == null) {
                    continue;
                }
                var file = el.getContainingFile();
                if (file == null) {
                    continue;
                }
                var vf = file.getVirtualFile();
                if (vf == null) {
                    continue;
                }
                resolvedPaths.add(vf.getPath());
            }

            Assertions.assertFalse(resolvedPaths.isEmpty(), "Reference resolved only to null elements");

            for (var f : expectedFiles) {
                Assertions.assertNotNull(f, "Expected file is null");
                var vf = f.getVirtualFile();
                Assertions.assertNotNull(vf, "Expected file has no VirtualFile: " + f);

                var expectedPath = vf.getPath();
                Assertions.assertTrue(
                        resolvedPaths.contains(expectedPath),
                        "Reference should resolve to " + expectedPath + ", but resolved to: " + resolvedPaths
                );
            }
        });
    }

    @Test
    public void testCacheInvalidationOnVcsIgnoredStatusChange() {
        // Create root and a utility file
        var root = myFixture.addFileToProject(
                "my-project/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        flows:
                          main:
                            - log: "Hello"
                        """);

        var utilsFile = myFixture.addFileToProject(
                "my-project/concord/utils.concord.yaml",
                """
                        flows:
                          utilsFlow:
                            - log: "Utils"
                        """);

        var service = ConcordScopeService.getInstance(getProject());
        var tracker = ConcordModificationTracker.getInstance(getProject());

        // Track which files are "ignored" - initially none
        Set<VirtualFile> ignoredFiles = new HashSet<>();
        service.setIgnoredFileChecker(ignoredFiles::contains);

        // Get initial search scope using PsiElement version (which uses caching)
        var initialScope = ReadAction.compute(() -> service.createSearchScope(root));
        Assertions.assertTrue(initialScope.contains(utilsFile.getVirtualFile()),
                "Initially, utils file should be in search scope");

        // Now "ignore" the utils file (simulates adding to .gitignore)
        ignoredFiles.add(utilsFile.getVirtualFile());

        // Simulate VCS change notification (in real scenario this happens via ChangeListListener)
        tracker.invalidate();

        // Get scope again - cache should be invalidated, ignored file should be excluded
        var updatedScope = ReadAction.compute(() -> service.createSearchScope(root));

        // After cache invalidation, ignored file should NOT be in scope
        Assertions.assertFalse(updatedScope.contains(utilsFile.getVirtualFile()),
                "After VCS change, ignored file should NOT be in search scope");
    }

    @Test
    public void testIgnoredRootDoesNotMaskNestedRoot() {
        // Create a root at the top level
        var root1 = myFixture.addFileToProject(
                "concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """);

        // Create a nested root
        var root2 = myFixture.addFileToProject(
                "concord/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """);

        var service = ConcordScopeService.getInstance(getProject());

        // Initially, root1 masks root2 (default behavior)
        var initialRoots = ReadAction.compute(service::findRoots);
        Assertions.assertEquals(1, initialRoots.size(), "Initially, only the top-level root should be found");
        Assertions.assertEquals(root1.getVirtualFile(), initialRoots.getFirst().getRootFile());

        // Now mark the top-level root as ignored
        Set<VirtualFile> ignoredFiles = new HashSet<>();
        ignoredFiles.add(root1.getVirtualFile());
        service.setIgnoredFileChecker(ignoredFiles::contains);

        // Invalidate cache to force re-computation of roots
        ConcordModificationTracker.getInstance(getProject()).invalidate();

        // Now root2 should be found because root1 is ignored and shouldn't mask it
        var updatedRoots = ReadAction.compute(service::findRoots);
        boolean foundRoot2 = updatedRoots.stream()
                .anyMatch(r -> r.getRootFile().equals(root2.getVirtualFile()));

        Assertions.assertTrue(foundRoot2, "Nested root should be found when parent root is ignored");
    }

    @Test
    public void testCacheInvalidationOnDirectoryRename() {
        var root = myFixture.addFileToProject(
                "my-project/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """);

        var service = ConcordScopeService.getInstance(getProject());
        var roots = ReadAction.compute(service::findRoots);
        Assertions.assertEquals(1, roots.size());
        var concordRoot = roots.getFirst();
        Assertions.assertTrue(concordRoot.getRootDir().toString().endsWith("my-project"));

        // Rename the directory
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                var dir = root.getVirtualFile().getParent();
                dir.rename(this, "my-project-renamed");
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Verify that roots are updated
        var updatedRoots = ReadAction.compute(service::findRoots);
        Assertions.assertEquals(1, updatedRoots.size());
        var updatedConcordRoot = updatedRoots.getFirst();

        Assertions.assertTrue(updatedConcordRoot.getRootDir().toString().endsWith("my-project-renamed"),
                "Root directory path should be updated after directory rename. Current path: " + updatedConcordRoot.getRootDir());
    }

    @Test
    public void testScopeUpdateOnDirectoryDeletion() {
        // Create top-level root
        var root = myFixture.addFileToProject(
                "concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """);

        // Create concord/ subdirectory with a concord file (matches default pattern)
        var nestedFile = myFixture.addFileToProject(
                "concord/concord.yml",
                """
                        flows:
                          nestedFlow:
                            - log: "Nested"
                        """);

        var service = ConcordScopeService.getInstance(getProject());

        // Top-level root should mask the nested root file
        var roots = ReadAction.compute(service::findRoots);
        Assertions.assertEquals(1, roots.size(),
                "Top-level root should mask nested root");

        // Nested file should be in scope (it's under the top-level root's default pattern)
        var nestedScopes = ReadAction.compute(() -> service.getScopesForFile(nestedFile.getVirtualFile()));
        Assertions.assertFalse(nestedScopes.isEmpty(),
                "Nested concord file should be in scope of top-level root");

        // Delete the entire concord/ directory
        var concordDir = nestedFile.getVirtualFile().getParent();
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                concordDir.delete(this);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        });

        // After deletion, the nested file should no longer be in any scope
        var filesInScope = ReadAction.compute(() -> service.getFilesInScope(roots.getFirst()));
        Assertions.assertEquals(1, filesInScope.size(),
                "Deleted file should no longer be in any scope");

        // Top-level root should still exist
        var updatedRoots = ReadAction.compute(service::findRoots);
        Assertions.assertEquals(1, updatedRoots.size(),
                "Top-level root should still exist after deleting concord/ directory");
        Assertions.assertEquals(root.getVirtualFile(), updatedRoots.getFirst().getRootFile(),
                "Remaining root should be the top-level concord.yaml");
    }

    /**
     * This test verifies that getScopesForFile correctly filters ignored files
     * after cache invalidation.
     */
    @Test
    public void testIgnoredFileFiltering() {
        var root = myFixture.addFileToProject(
                "my-project/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """);

        var utilsFile = myFixture.addFileToProject(
                "my-project/concord/utils.concord.yaml",
                "flows: {}");

        var service = ConcordScopeService.getInstance(getProject());
        var tracker = ConcordModificationTracker.getInstance(getProject());

        // Track ignored files
        Set<VirtualFile> ignoredFiles = new HashSet<>();
        service.setIgnoredFileChecker(ignoredFiles::contains);

        // Invalidate to apply new checker
        tracker.invalidate();

        // Initially not ignored - should have scopes
        var scopesBefore = ReadAction.compute(() -> service.getScopesForFile(utilsFile.getVirtualFile()));
        Assertions.assertEquals(1, scopesBefore.size(),
                "Non-ignored file should be in scope");

        // Now ignore the file
        ignoredFiles.add(utilsFile.getVirtualFile());

        // Invalidate cache to reflect ignored status change
        tracker.invalidate();

        var scopesAfter = ReadAction.compute(() -> service.getScopesForFile(utilsFile.getVirtualFile()));
        Assertions.assertTrue(scopesAfter.isEmpty(),
                "Ignored file should return empty scopes");
    }
}
