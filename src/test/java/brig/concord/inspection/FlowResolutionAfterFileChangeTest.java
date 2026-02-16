package brig.concord.inspection;

import brig.concord.navigation.FlowNamesIndex;
import brig.concord.psi.ConcordScopeService;
import brig.concord.psi.ProcessDefinitionProvider;
import brig.concord.psi.ref.FlowDefinitionReference;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.indexing.FileBasedIndex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Tests that flow resolution (and the "undefined flow" inspection) updates correctly
 * after file content changes — simulating scenarios like git branch switching.
 */
class FlowResolutionAfterFileChangeTest extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(ValueInspection.class);
    }

    /**
     * Diagnostic: checks each layer of the flow resolution chain after file content change.
     * This pinpoints exactly where the caching breaks.
     */
    @Test
    void testFlowResolutionChainAfterContentChange() {
        var mainFile = createFile("project-a/concord.yaml", """
                flows:
                  default:
                    - call: utilsFlow
                """);

        var utilsFile = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  otherFlow:
                    - log: "I am otherFlow"
                """);

        configureFromExistingFile(mainFile);

        // Trigger initial highlighting to populate caches
        myFixture.doHighlighting();

        // Record modification count before change
        var psiTracker = PsiModificationTracker.getInstance(getProject());
        long modCountBefore = psiTracker.getModificationCount();

        // Replace utils file content to add utilsFlow
        replaceFileContent(utilsFile, """
                flows:
                  utilsFlow:
                    - log: "I am utilsFlow"
                """);

        long modCountAfter = psiTracker.getModificationCount();

        // Step 1: PsiModificationTracker should have changed
        Assertions.assertTrue(modCountAfter > modCountBefore,
                "PsiModificationTracker should increment after setBinaryContent on another file." +
                        " Before=" + modCountBefore + ", After=" + modCountAfter);

        // Step 2: FileBasedIndex should contain the new key
        var indexKeys = FileBasedIndex.getInstance().getAllKeys(FlowNamesIndex.KEY, getProject());
        Assertions.assertTrue(indexKeys.contains("utilsFlow"),
                "FileBasedIndex should contain 'utilsFlow' after content change. All keys: " + indexKeys);

        // Step 3: Scope should include the utils file
        var scope = ConcordScopeService.getInstance(getProject()).createSearchScope(mainFile);
        Assertions.assertTrue(scope.contains(utilsFile.getVirtualFile()),
                "Search scope should contain utils file");

        // Step 4: ProcessDefinition should resolve the flow
        var process = ProcessDefinitionProvider.getInstance().get(mainFile);
        var flowNames = process.flowNames();
        Assertions.assertTrue(flowNames.contains("utilsFlow"),
                "flowNames() should contain 'utilsFlow'. Got: " + flowNames);

        var resolvedFlows = process.flows("utilsFlow");
        Assertions.assertFalse(resolvedFlows.isEmpty(),
                "flows('utilsFlow') should not be empty");
    }

    /**
     * Checks whether FlowDefinitionReference.multiResolve() returns stale cached results
     * after a cross-file content change.
     */
    @Test
    void testFlowDefinitionReferenceCacheInvalidation() {
        var mainFile = createFile("project-a/concord.yaml", """
                flows:
                  default:
                    - call: utilsFlow
                """);

        var utilsFile = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  otherFlow:
                    - log: "I am otherFlow"
                """);

        configureFromExistingFile(mainFile);

        // Trigger initial highlighting to populate FlowDefinitionReference cache
        myFixture.doHighlighting();

        // Get the reference from the call element
        var callValue = value("/flows/default/[0]/call");
        var psiElement = callValue.element();
        var refs = psiElement.getReferences();
        FlowDefinitionReference flowRef = null;
        for (var ref : refs) {
            if (ref instanceof FlowDefinitionReference fdr) {
                flowRef = fdr;
                break;
            }
        }
        Assertions.assertNotNull(flowRef, "Should have a FlowDefinitionReference");

        // Before change: multiResolve should return empty (utilsFlow not defined)
        var resultsBefore = flowRef.multiResolve(false);
        Assertions.assertEquals(0, resultsBefore.length,
                "Before change: utilsFlow should be unresolved");

        // Replace utils file content
        replaceFileContent(utilsFile, """
                flows:
                  utilsFlow:
                    - log: "I am utilsFlow"
                """);

        // After change: multiResolve should find utilsFlow
        var resultsAfter = flowRef.multiResolve(false);
        Assertions.assertTrue(resultsAfter.length > 0,
                "After change: utilsFlow should be resolved, but multiResolve returned " +
                        resultsAfter.length + " results (cached stale empty result?)");
    }

    /**
     * Simulates branch switch: a flow definition appears in another file after content change.
     * The call site should stop reporting "undefined flow" after the change.
     */
    @Test
    void testFlowAppearsInAnotherFileAfterContentChange() {
        var mainFile = createFile("project-a/concord.yaml", """
                flows:
                  default:
                    - call: utilsFlow
                """);

        var utilsFile = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  otherFlow:
                    - log: "I am otherFlow"
                """);

        configureFromExistingFile(mainFile);

        // utilsFlow doesn't exist yet -> should have "undefined flow" error
        inspection()
                .assertUndefinedFlow()
                .check();

        // Simulate branch switch: utils file now has utilsFlow
        replaceFileContent(utilsFile, """
                flows:
                  utilsFlow:
                    - log: "I am utilsFlow"
                """);

        assertNoErrors();
    }

    /**
     * Simulates branch switch: a flow definition disappears from another file after content change.
     * The call site should start reporting "undefined flow" after the change.
     */
    @Test
    void testFlowDisappearsFromAnotherFileAfterContentChange() {
        var mainFile = createFile("project-a/concord.yaml", """
                flows:
                  default:
                    - call: utilsFlow
                """);

        var utilsFile = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "I am utilsFlow"
                """);

        configureFromExistingFile(mainFile);

        // utilsFlow exists -> no errors
        assertNoErrors();

        // Simulate branch switch: utils file no longer has utilsFlow
        replaceFileContent(utilsFile, """
                flows:
                  renamedFlow:
                    - log: "utilsFlow was renamed"
                """);

        // After the change, utilsFlow should not be found -> error expected
        inspection()
                .assertUndefinedFlow()
                .check();
    }

    /**
     * Simulates branch switch: a new scope file with the target flow is added.
     */
    @Test
    void testNewFileWithFlowAddedToScope() {
        var mainFile = createFile("project-a/concord.yaml", """
                flows:
                  default:
                    - call: newFlow
                """);

        configureFromExistingFile(mainFile);

        // newFlow doesn't exist -> error
        inspection()
                .assertUndefinedFlow()
                .check();

        // Add new file to scope with the flow
        createFile("project-a/concord/new.concord.yaml", """
                flows:
                  newFlow:
                    - log: "I am newFlow"
                """);

        // After adding, newFlow should be found -> no errors
        assertNoErrors();
    }

    /**
     * Simulates branch switch: a scope file containing the target flow is deleted.
     */
    @Test
    void testFileWithFlowDeletedFromScope() {
        var mainFile = createFile("project-a/concord.yaml", """
                flows:
                  default:
                    - call: utilsFlow
                """);

        var utilsFile = createFile("project-a/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "I am utilsFlow"
                """);

        configureFromExistingFile(mainFile);

        // utilsFlow exists -> no errors
        assertNoErrors();

        // Simulate branch switch: utils file is deleted
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                utilsFile.getVirtualFile().delete(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // After deletion, utilsFlow should not be found -> error expected
        inspection()
                .assertUndefinedFlow()
                .check();
    }

    /**
     * Same-file scenario: flow definition appears after content change in the same file.
     */
    @Test
    void testFlowAppearsInSameFileAfterContentChange() {
        var mainFile = createFile("concord.yaml", """
                flows:
                  default:
                    - call: helperFlow
                """);

        configureFromExistingFile(mainFile);

        // helperFlow doesn't exist -> error
        inspection()
                .assertUndefinedFlow()
                .check();

        // Add helperFlow to the same file
        replaceFileContent(mainFile, """
                flows:
                  default:
                    - call: helperFlow
                  helperFlow:
                    - log: "I am helperFlow"
                """);

        // Need to reconfigure since the file we're inspecting changed
        configureFromExistingFile(mainFile);

        // After the change, helperFlow should be found -> no errors
        assertNoErrors();
    }

    private void replaceFileContent(PsiFile file, String newContent) {
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent(newContent.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
    }
}
