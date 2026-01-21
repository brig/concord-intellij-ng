package brig.concord.psi;

import brig.concord.ConcordYamlTestBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        var roots = service.findRoots();

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

        var scopes = service.getScopesForFile(vFile);
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
    public void testGetPrimaryScope() {
        var yaml = myFixture.addFileToProject(
                "concord.yaml",
                """
                        configuration:
                            runtime: concord-v2
                        """);

        var vFile = yaml.getVirtualFile();
        var service = ConcordScopeService.getInstance(getProject());

        var primaryScope = service.getPrimaryScope(vFile);
        Assertions.assertNotNull(primaryScope);
        Assertions.assertEquals("src", primaryScope.getScopeName());
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
        var roots = service.findRoots();

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
        var roots = service.findRoots();
        Assertions.assertEquals(1, roots.size());

        // Find the root for my-project
        var myProjectRoot = roots.getFirst();
        Assertions.assertNotNull(myProjectRoot, "Should find my-project root");

        // The nested file should be contained in the scope
        // Note: This test verifies the pattern matching works with the default concord pattern
        Assertions.assertTrue(myProjectRoot.contains(nestedFile.getVirtualFile()), "Nested file should be contained in scope");
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
        var roots = service.findRoots();
        Assertions.assertEquals(2, roots.size());

        // Find the root
        var narrowRoot = roots.stream()
                .filter(r -> r.getRootFile().equals(root.getVirtualFile()))
                .findFirst()
                .orElse(null);

        Assertions.assertNotNull(narrowRoot, "Should find narrow-project root");
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
        var scopeA = service.createSearchScope(rootA.getVirtualFile());

        // Verify scope-a contains project-a files
        Assertions.assertTrue(scopeA.contains(rootA.getVirtualFile()), "Scope A should contain rootA");
        Assertions.assertTrue(scopeA.contains(utilsA.getVirtualFile()), "Scope A should contain utilsA");

        // Verify scope-a does NOT contain project-b files
        Assertions.assertFalse(scopeA.contains(rootB.getVirtualFile()), "Scope A should NOT contain rootB");
        Assertions.assertFalse(scopeA.contains(utilsB.getVirtualFile()), "Scope A should NOT contain utilsB");

        // Get search scope for project-b
        var scopeB = service.createSearchScope(rootB.getVirtualFile());

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
}
