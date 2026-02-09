package brig.concord.psi;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.impl.NonBlockingReadActionImpl;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.PlatformTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.LongSupplier;

class ConcordModificationTrackerTest extends ConcordYamlTestBaseJunit5 {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();
        ConcordModificationTracker.getInstance(getProject()).setForceSyncInTests(false);
    }

    @Test
    void testStructureIncrementsOnConcordFileCreation() {
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        myFixture.addFileToProject("new-file.concord.yaml", "flows: {}");

        waitForIncrement(tracker.structure()::getModificationCount, initialStructure,
                "Structure count should increment on .concord.yaml creation");
    }

    @Test
    void testStructureIncrementsOnConcordFileDeletion() {
        var file = myFixture.addFileToProject("delete-me.concord.yaml", "flows: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().delete(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.structure()::getModificationCount, initialStructure,
                "Structure count should increment on .concord.yaml deletion");
    }

    @Test
    void testStructureIncrementsOnConcordFileRename() {
        var file = myFixture.addFileToProject("rename-me.concord.yaml", "flows: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().rename(this, "renamed.concord.yaml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.structure()::getModificationCount, initialStructure,
                "Structure count should increment on .concord.yaml rename");
    }

    @Test
    void testStructureIncrementsOnGitignoreCreation() {
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        myFixture.addFileToProject(".gitignore", "build/");

        waitForIncrement(tracker.structure()::getModificationCount, initialStructure,
                "Structure count should increment on .gitignore creation");
    }

    @Test
    void testStructureIncrementsOnDirectoryRename() {
        var file = myFixture.addFileToProject("my-dir/dummy.txt", "");
        VirtualFile dir = ReadAction.compute(() -> file.getParent().getVirtualFile());

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                dir.rename(this, "renamed-dir");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.structure()::getModificationCount, initialStructure,
                "Structure count should increment on directory rename");
    }

    @Test
    void testStructureIncrementsOnDirectoryMove() {
        var file1 = myFixture.addFileToProject("move-me/dummy.txt", "");
        var file2 = myFixture.addFileToProject("target-dir/dummy.txt", "");
        VirtualFile dir = ReadAction.compute(() -> file1.getParent().getVirtualFile());
        VirtualFile targetDir = ReadAction.compute(() -> file2.getParent().getVirtualFile());

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                dir.move(this, targetDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.structure()::getModificationCount, initialStructure,
                "Structure count should increment on directory move");
    }

    @Test
    void testNoChangeOnIrrelevantFileContentChange() {
        var file = myFixture.addFileToProject("irrelevant.txt", "content");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("new content".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        awaitProcessing();
        Assertions.assertEquals(initialStructure, tracker.structure().getModificationCount(),
                "Structure count should NOT increment on irrelevant file content change");
    }

    @Test
    void testModificationCountDoesNotIncrementOnConcordFileContentChange() {
        // ConcordModificationTracker explicitly ignores content changes as they are handled by PSI
        var file = myFixture.addFileToProject("test.concord.yaml", "flows: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialCount = tracker.structure().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("flows: { foo: bar }".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        awaitProcessing();
        Assertions.assertEquals(initialCount, tracker.structure().getModificationCount(),
                "Modification count should NOT increment on concord file content change in 'flows' section");
    }

    @Test
    void testModificationCountIncrementsOnGranularResourcesChange() {
        var file = myFixture.addFileToProject("concord.yaml", """
                resources:
                  concord:
                    - "glob:concord/*.yaml"
                """);

        openFileInEditor(file);

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        // Prime baseline without changing fingerprint
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var editor = myFixture.getEditor();
            var document = editor.getDocument();
            document.insertString(document.getTextLength(), "\n");
            com.intellij.psi.PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
        });

        awaitProcessing();
        long initialCount = tracker.structure().getModificationCount();

        // Perform a granular change: replace "concord/*.yaml" with "concord/**/*.yaml"
        // This simulates typing in the editor
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var editor = myFixture.getEditor();
            var text = editor.getDocument().getText();
            int offset = text.indexOf("glob:concord/*.yaml");
            editor.getDocument().replaceString(offset + 13, offset + 14, "**");

            // Commit changes to update PSI
            com.intellij.psi.PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
        });

        waitForIncrement(tracker.structure()::getModificationCount, initialCount,
                "Modification count SHOULD increment on granular change in 'resources' section");
    }

    @Test
    void testScopeIncrementsOnRootFileContentChange() {
        // Root file (concord.yaml, concord.yml) content changes affect scope (patterns may change)
        var file = myFixture.addFileToProject("concord.yaml", "configuration: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        // Prime baseline without changing fingerprint (no deps/resources)
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("configuration: {}\n".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("configuration:\n  concord:\n    - \"glob:**/*.concord.yaml\"".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        awaitProcessing();
        Assertions.assertEquals(initialStructure, tracker.structure().getModificationCount(),
                "Structure count should NOT increment on root file content change");
    }

    @Test
    void testStructureIncrementsOnRenameFromIrrelevantToRelevant() {
        var file = myFixture.addFileToProject("temp.txt", "flows: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().rename(this, "new.concord.yaml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.structure()::getModificationCount, initialStructure,
                "Structure count should increment when renaming to relevant file");
    }

    @Test
    void testStructureIncrementsOnRenameFromRelevantToIrrelevant() {
        var file = myFixture.addFileToProject("old.concord.yaml", "flows: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().rename(this, "temp.txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.structure()::getModificationCount, initialStructure,
                "Structure count should increment when renaming from relevant file");
    }

    @Test
    void testDependenciesIncrementOnNewRootWithDependencies() throws Exception {
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialDeps = tracker.dependencies().getModificationCount();

        myFixture.addFileToProject("concord.yaml", """
                configuration:
                  dependencies:
                    - msvn://org.example:demo:1.0
                """);

        EdtTestUtil.runInEdtAndWait(() ->
                com.intellij.psi.PsiDocumentManager.getInstance(getProject()).commitAllDocuments()
        );

        waitForIncrement(tracker.dependencies()::getModificationCount, initialDeps,
                "Dependencies count should increment when a new root with dependencies is created");
    }

    @Test
    void testDependenciesIncrementOnRootProfileDependenciesChange() throws Exception {
        var file = myFixture.addFileToProject("concord.yaml", """
                profiles:
                  dev:
                    configuration:
                      dependencies: []
                """);

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialDeps = tracker.dependencies().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("""
                        profiles:
                          dev:
                            configuration:
                              dependencies:
                                - msvn://org.example:demo:2.0
                        """.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.dependencies()::getModificationCount, initialDeps,
                "Dependencies count should increment on root profile dependencies change");
    }

    @Test
    void testDependenciesIncrementOnNonRootDependenciesChange() throws Exception {
        var file = myFixture.addFileToProject("other.concord.yaml", """
                configuration:
                  dependencies:
                    - msvn://org.example:demo:1.0
                """);

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialDeps = tracker.dependencies().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("""
                        configuration:
                          dependencies:
                            - msvn://org.example:demo:1.1
                        """.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.dependencies()::getModificationCount, initialDeps,
                "Dependencies count should increment on non-root concord file dependencies change");
    }

    @Test
    void testDependenciesIncrementOnNonRootExtraDependenciesChange() throws Exception {
        var file = myFixture.addFileToProject("other.concord.yaml", """
                configuration:
                  extraDependencies:
                    - msvn://org.example:demo:1.0
                """);

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialDeps = tracker.dependencies().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("""
                        configuration:
                          extraDependencies:
                            - msvn://org.example:demo:1.1
                        """.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.dependencies()::getModificationCount, initialDeps,
                "Dependencies count should increment on non-root concord file extraDependencies change");
    }

    @Test
    void testDependenciesIncrementOnNonRootProfileDependenciesChange() throws Exception {
        var file = myFixture.addFileToProject("other.concord.yaml", """
                profiles:
                  dev:
                    configuration:
                      dependencies: []
                """);

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialDeps = tracker.dependencies().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("""
                        profiles:
                          dev:
                            configuration:
                              dependencies:
                                - msvn://org.example:demo:1.2
                        """.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.dependencies()::getModificationCount, initialDeps,
                "Dependencies count should increment on non-root concord file profile dependencies change");
    }

    @Test
    void testDependenciesIncrementOnNonRootProfileExtraDependenciesChange() throws Exception {
        var file = myFixture.addFileToProject("other.concord.yaml", """
                profiles:
                  dev:
                    configuration:
                      extraDependencies: []
                """);

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialDeps = tracker.dependencies().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("""
                        profiles:
                          dev:
                            configuration:
                              extraDependencies:
                                - msvn://org.example:demo:1.2
                        """.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.dependencies()::getModificationCount, initialDeps,
                "Dependencies count should increment on non-root concord file profile extraDependencies change");
    }

    @Test
    void testDependenciesDoNotIncrementWhenFileIsInitiallyInvalid() {
        var file = myFixture.addFileToProject("bad.concord.yaml", """
            configuration:
              dependencies:
                - mvn: //org.example:demo:1.0
                  some: trash
            """);

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialDeps = tracker.dependencies().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("""
                    configuration:
                      dependencies:
                        - mvn: //org.example:demo:1.0
                          some: trash
                          changed: again
                    """.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        awaitProcessing();
        Assertions.assertEquals(initialDeps, tracker.dependencies().getModificationCount(),
                "No increment expected: file never had a valid fingerprint in cache");
    }

    @Test
    void testDependenciesIncrementOnNonRootFileDeletion() {
        var file = myFixture.addFileToProject("delete-deps.concord.yaml", """
                configuration:
                  dependencies:
                    - msvn://org.example:demo:1.0
                """);

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialDeps = tracker.dependencies().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().delete(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.dependencies()::getModificationCount, initialDeps,
                "Dependencies count should increment on non-root concord file deletion if it had dependencies");
    }

    @Test
    void testDependenciesIncrementOnNonRootRenameToIrrelevant() {
        var file = myFixture.addFileToProject("rename-deps.concord.yaml", """
                configuration:
                  dependencies:
                    - msvn://org.example:demo:1.0
                """);

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialDeps = tracker.dependencies().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().rename(this, "rename-deps.txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        waitForIncrement(tracker.dependencies()::getModificationCount, initialDeps,
                "Dependencies count should increment on non-root concord file rename to irrelevant");
    }

    @Test
    void testForceRefreshIncrementsStructureAndNotifiesListener() {
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        var notified = new AtomicBoolean(false);
        getProject().getMessageBus().connect(myFixture.getTestRootDisposable())
                .subscribe(ConcordProjectListener.TOPIC, () -> notified.set(true));

        tracker.forceRefresh();

        Assertions.assertTrue(tracker.structure().getModificationCount() > initialStructure,
                "forceRefresh() should increment structure modification count");

        awaitProcessing();
        Assertions.assertTrue(notified.get(),
                "forceRefresh() should trigger ConcordProjectListener.projectChanged()");
    }

    @Test
    void testStructureDoesNotIncrementOnNonRootResourcesChange() {
        var file = myFixture.addFileToProject("non-root.concord.yaml", "flows: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        awaitProcessing();
        long initialStructure = tracker.structure().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                // Adding resources section to a non-root file should be ignored by structure tracker
                file.getVirtualFile().setBinaryContent("""
                        resources:
                          concord:
                            - "glob:*.txt"
                        flows: {}
                        """.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        awaitProcessing();
        Assertions.assertEquals(initialStructure, tracker.structure().getModificationCount(),
                "Structure count should NOT increment on resources change in non-root file");
    }

    private static void awaitProcessing() {
        EdtTestUtil.runInEdtAndWait(() -> {
            // 1) дать отработать queued updates / invokeLater / MergingUpdateQueue
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
            // 2) дождаться завершения non-blocking read actions
            NonBlockingReadActionImpl.waitForAsyncTaskCompletion();
            // 3) дать выполниться finishOnUiThread/applyBatchResult
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
            // 4) на всякий случай второй проход: applyBatchResult мог снова enqueue update
            NonBlockingReadActionImpl.waitForAsyncTaskCompletion();
            PlatformTestUtil.dispatchAllEventsInIdeEventQueue();
        });
    }

    private static void waitForIncrement(LongSupplier supplier, long initialValue, String message) {
        EdtTestUtil.runInEdtAndWait(() ->
            PlatformTestUtil.waitWithEventsDispatching(message, () -> {
                awaitProcessing();
                return supplier.getAsLong() > initialValue;
            }, 5_000));
    }
}
