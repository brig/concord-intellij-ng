package brig.concord.dependency;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.psi.ConcordModificationTracker;
import com.intellij.openapi.command.WriteCommandAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class DependencyChangeTrackerTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void needsReloadReturnsFalseWhenNeverLoaded() {
        // Given: fresh project, no initial load
        var tracker = DependencyChangeTracker.getInstance(getProject());
        // Reset to "never loaded" state (startup activity may have run)
        tracker.setStateForTest(null, false);

        // Then: should not need reload (nothing to compare against)
        Assertions.assertFalse(tracker.needsReload(),
                "needsReload should be false when dependencies were never loaded");
    }

    @Test
    void needsReloadReturnsFalseAfterInitialLoad() {
        createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:1.0.0"
                """);

        var tracker = DependencyChangeTracker.getInstance(getProject());
        var dep = MavenCoordinate.parse("mvn://com.example:lib:1.0.0");

        Assertions.assertNotNull(dep);

        // When: initial load
        tracker.markInitialLoad(Set.of(dep));

        // Then: should not need reload
        Assertions.assertFalse(tracker.needsReload(),
                "needsReload should be false after initial load");
    }

    @Test
    void needsReloadReturnsTrueAfterDependencyChange() {
        var file = createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:old:1.0.0"
                """);

        var tracker = DependencyChangeTracker.getInstance(getProject());
        var oldDep = MavenCoordinate.parse("mvn://com.example:old:1.0.0");
        Assertions.assertNotNull(oldDep);

        // Reset state after file creation (file creation triggers events)
        tracker.setStateForTest(Set.of(oldDep), false);
        Assertions.assertFalse(tracker.needsReload());

        // Open file and change dependency
        openFileInEditor(file);
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var editor = myFixture.getEditor();
            var text = editor.getDocument().getText();
            int offset = text.indexOf("old:1.0.0");
            editor.getDocument().replaceString(offset, offset + 9, "new:2.0.0");
            com.intellij.psi.PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
        });

        // Directly trigger scheduleCheck (in production, this is wired via TOPIC subscription)
        tracker.checkForChangesNow();

        // Then: should need reload
        Assertions.assertTrue(tracker.needsReload(),
                "needsReload should be true after dependency content changed");
    }

    @Test
    void needsReloadReturnsFalseAfterWhitespaceOnlyChange() {
        var file = createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:lib:1.0.0"
                """);

        var tracker = DependencyChangeTracker.getInstance(getProject());
        var dep = MavenCoordinate.parse("mvn://com.example:lib:1.0.0");
        Assertions.assertNotNull(dep);

        // Reset state after file creation (file creation triggers events)
        tracker.setStateForTest(Set.of(dep), false);
        Assertions.assertFalse(tracker.needsReload());

        // Add whitespace (doesn't change actual dependencies)
        openFileInEditor(file);
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var editor = myFixture.getEditor();
            var text = editor.getDocument().getText();
            // Add trailing spaces after the dependency line
            int offset = text.indexOf("1.0.0\"") + 6;
            editor.getDocument().insertString(offset, "   ");
            com.intellij.psi.PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
        });

        // Directly trigger scheduleCheck
        tracker.checkForChangesNow();

        // Then: should NOT need reload (same deps after parsing)
        Assertions.assertFalse(tracker.needsReload(),
                "needsReload should be false after whitespace-only change (deps unchanged)");
    }

    @Test
    void needsReloadReturnsFalseAfterMarkReloaded() {
        var tracker = DependencyChangeTracker.getInstance(getProject());
        var oldDep = MavenCoordinate.parse("mvn://com.example:old:1.0.0");
        Assertions.assertNotNull(oldDep);
        var newDep = MavenCoordinate.parse("mvn://com.example:new:2.0.0");
        Assertions.assertNotNull(newDep);

        // Setup: initial load, then simulate dirty state
        tracker.markInitialLoad(Set.of(oldDep));
        tracker.setStateForTest(Set.of(oldDep), true); // Force dirty

        Assertions.assertTrue(tracker.needsReload());

        // When: mark as reloaded
        var modCount = tracker.getCurrentModCount();
        tracker.markReloaded(Set.of(newDep), modCount);

        // Then: should not need reload
        Assertions.assertFalse(tracker.needsReload(),
                "needsReload should be false after markReloaded");
    }

    @Test
    void needsReloadReturnsFalseAfterDismiss() {
        var tracker = DependencyChangeTracker.getInstance(getProject());
        var dep = MavenCoordinate.parse("mvn://com.example:lib:1.0.0");
        Assertions.assertNotNull(dep);

        // Setup: dirty state
        tracker.markInitialLoad(Set.of(dep));
        tracker.setStateForTest(Set.of(dep), true); // Force dirty

        Assertions.assertTrue(tracker.needsReload());

        // When: dismiss
        tracker.dismiss();

        // Then: should not need reload
        Assertions.assertFalse(tracker.needsReload(),
                "needsReload should be false after dismiss");
    }

    @Test
    void needsReloadReturnsTrueAfterDismissAndNewChange() {
        createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:v1:1.0.0"
                """);

        var tracker = DependencyChangeTracker.getInstance(getProject());
        var v1Dep = MavenCoordinate.parse("mvn://com.example:v1:1.0.0");
        Assertions.assertNotNull(v1Dep);

        // Initial load and force dirty
        tracker.markInitialLoad(Set.of(v1Dep));
        tracker.setStateForTest(Set.of(v1Dep), true);

        // Dismiss
        tracker.dismiss();
        Assertions.assertFalse(tracker.needsReload());

        // Simulate a dependency change by incrementing the dependencies modification count.
        // In production, editing the file triggers ConcordModificationTracker -> TOPIC.
        // Here we use forceSyncInTests=true (default from base class), so
        // ConcordModificationTracker.dependencies().getModificationCount() is already
        // incremented synchronously on file creation. We just need to increment again.
        ConcordModificationTracker.getInstance(getProject()).invalidateDependencies();

        // The modCount incremented — dismissed modCount is now stale
        Assertions.assertTrue(tracker.needsReload(),
                "needsReload should be true after dismiss + new change (modCount changed)");
    }

    @Test
    void markReloadedDoesNotClearDirtyWhenModCountChanged() {
        var tracker = DependencyChangeTracker.getInstance(getProject());
        var dep = MavenCoordinate.parse("mvn://com.example:lib:1.0.0");
        Assertions.assertNotNull(dep);

        // Initial load
        tracker.markInitialLoad(Set.of(dep));

        // Capture modCount before change
        long modCountBefore = tracker.getCurrentModCount();

        // Force dirty state
        tracker.setStateForTest(Set.of(dep), true);
        Assertions.assertTrue(tracker.needsReload());

        // Simulate: during reload, modCount changed (user edited while reloading)
        // Creating a new concord file triggers modCount increment via forceSyncInTests
        createFile("other.concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:other:1.0.0"
                """);

        // Now modCount > modCountBefore
        Assertions.assertTrue(tracker.getCurrentModCount() > modCountBefore,
                "modCount should have increased after file creation");

        // When: markReloaded with stale modCount
        tracker.markReloaded(Set.of(dep), modCountBefore);

        // Then: dirty should still be true (race condition protection)
        // markReloaded detects modCount mismatch and calls scheduleCheck instead of clearing dirty.
        // Since dirty is already true, scheduleCheck returns early. The dirty flag remains true.
        Assertions.assertTrue(tracker.needsReload(),
                "needsReload should still be true when markReloaded called with stale modCount");
    }

    @Test
    void scheduleCheckSkipsWhenAlreadyDirty() {
        var tracker = DependencyChangeTracker.getInstance(getProject());
        var dep = MavenCoordinate.parse("mvn://com.example:lib:1.0.0");
        Assertions.assertNotNull(dep);

        // Initial load and force dirty
        tracker.markInitialLoad(Set.of(dep));
        tracker.setStateForTest(Set.of(dep), true);

        // When: scheduleCheck called while dirty
        tracker.scheduleCheck();

        // Then: should still be dirty (no-op when already dirty)
        Assertions.assertTrue(tracker.needsReload(),
                "scheduleCheck should be no-op when already dirty");
    }

    @Test
    void scheduleCheckSkipsWhenNeverLoaded() {
        var tracker = DependencyChangeTracker.getInstance(getProject());
        // Reset to "never loaded" state (startup activity may have run)
        tracker.setStateForTest(null, false);

        // When: scheduleCheck called before initial load
        tracker.scheduleCheck();

        // Then: should not need reload (nothing to compare)
        Assertions.assertFalse(tracker.needsReload(),
                "scheduleCheck should be no-op when never loaded");
    }

    @Test
    void addingDependencyTriggersDirtyFlag() {
        var file = createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:lib1:1.0.0"
                """);

        var tracker = DependencyChangeTracker.getInstance(getProject());
        var lib1 = MavenCoordinate.parse("mvn://com.example:lib1:1.0.0");
        Assertions.assertNotNull(lib1);

        // Reset state after file creation (file creation triggers events)
        tracker.setStateForTest(Set.of(lib1), false);
        Assertions.assertFalse(tracker.needsReload());

        // Add second dependency
        openFileInEditor(file);
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var editor = myFixture.getEditor();
            var text = editor.getDocument().getText();
            int offset = text.indexOf("lib1:1.0.0\"") + 11;
            editor.getDocument().insertString(offset, "\n    - \"mvn://com.example:lib2:1.0.0\"");
            com.intellij.psi.PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
        });

        // Directly trigger scheduleCheck (in production, wired via TOPIC subscription)
        tracker.checkForChangesNow();

        Assertions.assertTrue(tracker.needsReload(),
                "needsReload should be true after adding a dependency");
    }

    @Test
    void removingDependencyTriggersDirtyFlag() {
        var file = createFile("concord.yaml", """
                configuration:
                  dependencies:
                    - "mvn://com.example:lib1:1.0.0"
                    - "mvn://com.example:lib2:1.0.0"
                """);

        var tracker = DependencyChangeTracker.getInstance(getProject());
        var lib1 = MavenCoordinate.parse("mvn://com.example:lib1:1.0.0");
        Assertions.assertNotNull(lib1);
        var lib2 = MavenCoordinate.parse("mvn://com.example:lib2:1.0.0");
        Assertions.assertNotNull(lib2);

        // Reset state after file creation (file creation triggers events)
        tracker.setStateForTest(Set.of(lib1, lib2), false);
        Assertions.assertFalse(tracker.needsReload());

        // Remove second dependency
        openFileInEditor(file);
        WriteCommandAction.runWriteCommandAction(getProject(), () -> {
            var editor = myFixture.getEditor();
            var text = editor.getDocument().getText();
            int start = text.indexOf("\n    - \"mvn://com.example:lib2");
            int end = text.indexOf("lib2:1.0.0\"") + 11;
            editor.getDocument().deleteString(start, end);
            com.intellij.psi.PsiDocumentManager.getInstance(getProject()).commitAllDocuments();
        });

        // Directly trigger scheduleCheck (in production, wired via TOPIC subscription)
        tracker.checkForChangesNow();

        Assertions.assertTrue(tracker.needsReload(),
                "needsReload should be true after removing a dependency");
    }

}
