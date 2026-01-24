package brig.concord.notification;

import brig.concord.ConcordYamlTestBase;
import com.intellij.openapi.application.ReadAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OutOfScopeEditorNotificationProviderTest extends ConcordYamlTestBase {

    @Test
    public void testRootFile_noNotification() {
        var file = myFixture.addFileToProject("concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - log: "Hello"
                """);

        var provider = new OutOfScopeEditorNotificationProvider();
        var result = ReadAction.compute(() ->
                provider.collectNotificationData(getProject(), file.getVirtualFile()));

        Assertions.assertNull(result, "Root file should not show notification");
    }

    @Test
    public void testFileInScope_noNotification() {
        // Create root that defines the scope
        myFixture.addFileToProject("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                """);

        // Create a file that matches the default concord/** pattern
        var utilsFile = myFixture.addFileToProject("project-a/concord/utils.concord.yaml", """
                flows:
                  utilsFlow:
                    - log: "Utils"
                """);

        var provider = new OutOfScopeEditorNotificationProvider();
        var result = ReadAction.compute(() ->
                provider.collectNotificationData(getProject(), utilsFile.getVirtualFile()));

        Assertions.assertNull(result, "File in scope should not show notification");
    }

    @Test
    public void testFileOutOfScope_showsNotification() {
        // Create root with restricted scope
        myFixture.addFileToProject("project-a/concord.yaml", """
                resources:
                  concord:
                    - "glob:flows/*.concord.yaml"
                configuration:
                  runtime: concord-v2
                """);

        // Create a file outside the scope pattern
        var orphanFile = myFixture.addFileToProject("project-a/other/orphan.concord.yaml", """
                flows:
                  orphanFlow:
                    - log: "Orphan"
                """);

        var provider = new OutOfScopeEditorNotificationProvider();
        var result = ReadAction.compute(() ->
                provider.collectNotificationData(getProject(), orphanFile.getVirtualFile()));

        Assertions.assertNotNull(result, "File out of scope should show notification");
    }

    @Test
    public void testDotConcordYamlRoot_noNotification() {
        var file = myFixture.addFileToProject(".concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - log: "Hello"
                """);

        var provider = new OutOfScopeEditorNotificationProvider();
        var result = ReadAction.compute(() ->
                provider.collectNotificationData(getProject(), file.getVirtualFile()));

        Assertions.assertNull(result, "Root file (.concord.yaml) should not show notification");
    }

    @Test
    public void testNonConcordFile_noNotification() {
        var file = myFixture.addFileToProject("some-file.yaml", """
                some:
                  key: value
                """);

        var provider = new OutOfScopeEditorNotificationProvider();
        var result = ReadAction.compute(() ->
                provider.collectNotificationData(getProject(), file.getVirtualFile()));

        Assertions.assertNull(result, "Non-Concord file should not show notification");
    }
}
