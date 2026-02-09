package brig.concord.navigation;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.openapi.application.ReadAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class ConcordRootGotoRelatedProviderTest extends ConcordYamlTestBaseJunit5 {

    private final ConcordRootGotoRelatedProvider provider = new ConcordRootGotoRelatedProvider();

    @Test
    void testReturnsRootForFileInScope() {
        // Create root file
        var root = myFixture.addFileToProject(
                "my-project/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """);

        // Create file in scope (default pattern includes concord/*.concord.yaml)
        var utilsFile = myFixture.addFileToProject(
                "my-project/concord/utils.concord.yaml",
                """
                        flows:
                          utilFlow:
                            - log: "Hello"
                        """);

        myFixture.configureFromExistingVirtualFile(utilsFile.getVirtualFile());

        var items = ReadAction.compute(() -> provider.getItems(utilsFile));

        Assertions.assertEquals(1, items.size());
        var item = items.get(0);
        Assertions.assertEquals("my-project", item.getCustomName());
        Assertions.assertEquals("concord.yaml", item.getCustomContainerName());
    }

    @Test
    void testReturnsEmptyForRootFile() {
        var root = myFixture.addFileToProject(
                "my-project/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """);

        myFixture.configureFromExistingVirtualFile(root.getVirtualFile());

        var items = ReadAction.compute(() -> provider.getItems(root));

        Assertions.assertTrue(items.isEmpty(), "Root file should not have related roots");
    }

    @Test
    void testReturnsEmptyForOutOfScopeFile() {
        // Create root with narrow pattern
        myFixture.addFileToProject(
                "my-project/concord.yaml",
                """
                        resources:
                          concord:
                            - "glob:flows/*.concord.yaml"
                        """);

        // Create file outside the pattern
        var outsideFile = myFixture.addFileToProject(
                "my-project/other/utils.concord.yaml",
                """
                        flows:
                          utilFlow:
                            - log: "Hello"
                        """);

        myFixture.configureFromExistingVirtualFile(outsideFile.getVirtualFile());

        var items = ReadAction.compute(() -> provider.getItems(outsideFile));

        Assertions.assertTrue(items.isEmpty(), "Out-of-scope file should not have related roots");
    }

    @Test
    void testReturnsMultipleRootsWhenFileInMultipleScopes() {
        // Root 1 includes files in sub/shared/
        myFixture.addFileToProject(
                "concord.yaml",
                """
                        resources:
                          concord:
                            - "glob:sub/shared/*.concord.yaml"
                        """);

        // Root 2 in sub/, includes files in shared/
        myFixture.addFileToProject(
                "sub/concord.yaml",
                """
                        resources:
                          concord:
                            - "glob:shared/*.concord.yaml"
                        """);

        // The shared file matches both patterns
        var sharedFile = myFixture.addFileToProject(
                "sub/shared/utils.concord.yaml",
                "flows: {}");

        myFixture.configureFromExistingVirtualFile(sharedFile.getVirtualFile());

        var items = ReadAction.compute(() -> provider.getItems(sharedFile));

        Assertions.assertEquals(2, items.size(), "File should be related to both roots");

        var names = items.stream()
                .map(GotoRelatedItem::getCustomName)
                .sorted()
                .toList();

        Assertions.assertEquals(List.of("src", "sub"), names);
    }

    @Test
    void testNavigatesToCorrectFile() {
        var root = myFixture.addFileToProject(
                "my-project/concord.yaml",
                """
                        configuration:
                          runtime: concord-v2
                        """);

        var utilsFile = myFixture.addFileToProject(
                "my-project/concord/utils.concord.yaml",
                "flows: {}");

        myFixture.configureFromExistingVirtualFile(utilsFile.getVirtualFile());

        var items = ReadAction.compute(() -> provider.getItems(utilsFile));

        Assertions.assertEquals(1, items.size());
        var item = items.get(0);

        var element = item.getElement();
        Assertions.assertNotNull(element);
        Assertions.assertEquals(root.getVirtualFile(), element.getContainingFile().getVirtualFile());
    }
}
