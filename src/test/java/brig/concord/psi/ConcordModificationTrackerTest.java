package brig.concord.psi;

import brig.concord.ConcordYamlTestBase;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ConcordModificationTrackerTest extends ConcordYamlTestBase {

    @Test
    public void testStructureIncrementsOnConcordFileCreation() {
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        long initialStructure = tracker.structure().getModificationCount();
        long initialScope = tracker.scope().getModificationCount();

        myFixture.addFileToProject("new-file.concord.yaml", "flows: {}");

        Assertions.assertTrue(tracker.structure().getModificationCount() > initialStructure,
                "Structure count should increment on .concord.yaml creation");
        Assertions.assertTrue(tracker.scope().getModificationCount() > initialScope,
                "Scope count should increment on .concord.yaml creation");
    }

    @Test
    public void testStructureIncrementsOnConcordFileDeletion() {
        var file = myFixture.addFileToProject("delete-me.concord.yaml", "flows: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        long initialStructure = tracker.structure().getModificationCount();
        long initialScope = tracker.scope().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().delete(this);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertTrue(tracker.structure().getModificationCount() > initialStructure,
                "Structure count should increment on .concord.yaml deletion");
        Assertions.assertTrue(tracker.scope().getModificationCount() > initialScope,
                "Scope count should increment on .concord.yaml deletion");
    }

    @Test
    public void testStructureIncrementsOnConcordFileRename() {
        var file = myFixture.addFileToProject("rename-me.concord.yaml", "flows: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        long initialStructure = tracker.structure().getModificationCount();
        long initialScope = tracker.scope().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().rename(this, "renamed.concord.yaml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertTrue(tracker.structure().getModificationCount() > initialStructure,
                "Structure count should increment on .concord.yaml rename");
        Assertions.assertTrue(tracker.scope().getModificationCount() > initialScope,
                "Scope count should increment on .concord.yaml rename");
    }

    @Test
    public void testStructureIncrementsOnGitignoreCreation() {
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        long initialStructure = tracker.structure().getModificationCount();
        long initialScope = tracker.scope().getModificationCount();

        myFixture.addFileToProject(".gitignore", "build/");

        Assertions.assertTrue(tracker.structure().getModificationCount() > initialStructure,
                "Structure count should increment on .gitignore creation");
        Assertions.assertTrue(tracker.scope().getModificationCount() > initialScope,
                "Scope count should increment on .gitignore creation");
    }

    @Test
    public void testStructureIncrementsOnDirectoryRename() {
        var file = myFixture.addFileToProject("my-dir/dummy.txt", "");
        VirtualFile dir = ReadAction.compute(() -> file.getParent().getVirtualFile());

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        long initialStructure = tracker.structure().getModificationCount();
        long initialScope = tracker.scope().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                dir.rename(this, "renamed-dir");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertTrue(tracker.structure().getModificationCount() > initialStructure,
                "Structure count should increment on directory rename");
        Assertions.assertTrue(tracker.scope().getModificationCount() > initialScope,
                "Scope count should increment on directory rename");
    }

    @Test
    public void testStructureIncrementsOnDirectoryMove() {
        var file1 = myFixture.addFileToProject("move-me/dummy.txt", "");
        var file2 = myFixture.addFileToProject("target-dir/dummy.txt", "");
        VirtualFile dir = ReadAction.compute(() -> file1.getParent().getVirtualFile());
        VirtualFile targetDir = ReadAction.compute(() -> file2.getParent().getVirtualFile());

        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        long initialStructure = tracker.structure().getModificationCount();
        long initialScope = tracker.scope().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                dir.move(this, targetDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertTrue(tracker.structure().getModificationCount() > initialStructure,
                "Structure count should increment on directory move");
        Assertions.assertTrue(tracker.scope().getModificationCount() > initialScope,
                "Scope count should increment on directory move");
    }

    @Test
    public void testNoChangeOnIrrelevantFileContentChange() {
        var file = myFixture.addFileToProject("irrelevant.txt", "content");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        long initialStructure = tracker.structure().getModificationCount();
        long initialScope = tracker.scope().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("new content".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertEquals(initialStructure, tracker.structure().getModificationCount(),
                "Structure count should NOT increment on irrelevant file content change");
        Assertions.assertEquals(initialScope, tracker.scope().getModificationCount(),
                "Scope count should NOT increment on irrelevant file content change");
    }

    @Test
    public void testNoStructureChangeOnNonRootConcordFileContentChange() {
        // Non-root concord file content changes don't affect structure or scope
        var file = myFixture.addFileToProject("test.concord.yaml", "flows: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        long initialStructure = tracker.structure().getModificationCount();
        long initialScope = tracker.scope().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("flows: { foo: bar }".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertEquals(initialStructure, tracker.structure().getModificationCount(),
                "Structure count should NOT increment on non-root concord file content change");
        Assertions.assertEquals(initialScope, tracker.scope().getModificationCount(),
                "Scope count should NOT increment on non-root concord file content change (not a root file)");
    }

    @Test
    public void testScopeIncrementsOnRootFileContentChange() {
        // Root file (concord.yaml, concord.yml) content changes affect scope (patterns may change)
        var file = myFixture.addFileToProject("concord.yaml", "configuration: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        long initialStructure = tracker.structure().getModificationCount();
        long initialScope = tracker.scope().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().setBinaryContent("resources:\n  concord:\n    - \"glob:**/*.concord.yaml\"".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertEquals(initialStructure, tracker.structure().getModificationCount(),
                "Structure count should NOT increment on root file content change");
        Assertions.assertTrue(tracker.scope().getModificationCount() > initialScope,
                "Scope count should increment on root file content change (patterns may change)");
    }

    @Test
    public void testStructureIncrementsOnRenameFromIrrelevantToRelevant() {
        var file = myFixture.addFileToProject("temp.txt", "flows: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        long initialStructure = tracker.structure().getModificationCount();
        long initialScope = tracker.scope().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().rename(this, "new.concord.yaml");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertTrue(tracker.structure().getModificationCount() > initialStructure,
                "Structure count should increment when renaming to relevant file");
        Assertions.assertTrue(tracker.scope().getModificationCount() > initialScope,
                "Scope count should increment when renaming to relevant file");
    }

    @Test
    public void testStructureIncrementsOnRenameFromRelevantToIrrelevant() {
        var file = myFixture.addFileToProject("old.concord.yaml", "flows: {}");
        ConcordModificationTracker tracker = ConcordModificationTracker.getInstance(getProject());
        long initialStructure = tracker.structure().getModificationCount();
        long initialScope = tracker.scope().getModificationCount();

        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            try {
                file.getVirtualFile().rename(this, "temp.txt");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        Assertions.assertTrue(tracker.structure().getModificationCount() > initialStructure,
                "Structure count should increment when renaming from relevant file");
        Assertions.assertTrue(tracker.scope().getModificationCount() > initialScope,
                "Scope count should increment when renaming from relevant file");
    }
}