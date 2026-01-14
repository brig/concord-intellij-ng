package brig.concord;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class ConcordStructureViewTest extends BasePlatformTestCase {

    @BeforeEach
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected String getTestDataPath() {
        return "./src/test/resources/structureView";
    }

    @Test
    public void testStructureViewFull() {
        myFixture.configureByFile("structure-view.concord.yaml");

        EdtTestUtil.runInEdtAndWait(() -> {
            myFixture.testStructureView(svc -> {
                var root = svc.getTreeModel().getRoot();
                assertNotNull(root);

//                printStructure(root, 0);

                var children = root.getChildren();
                assertNotNull(children);

                var configuration = assertElement(children, "configuration");
                assertEquals(1, configuration.getChildren().length);

                var flows = assertElement(children, "flows");
                assertEquals(2, flows.getChildren().length);

                var forms =  assertElement(children, "forms");
                assertEquals(1, forms.getChildren().length);

                var profiles =  assertElement(children, "profiles");
                assertEquals(1, profiles.getChildren().length);

                var resources =  assertElement(children, "resources");
                assertEquals(1, resources.getChildren().length);

                var imports =  assertElement(children, "imports");
                assertEquals(2, imports.getChildren().length);

                var triggers =  assertElement(children, "triggers");
                assertEquals(1, triggers.getChildren().length);

                var publicFlows =  assertElement(children, "publicFlows");
                assertEquals(2, publicFlows.getChildren().length);
            });
        });
    }

    @Test
    public void testStructureViewEmpty() {
        myFixture.configureByFile("structure-view-empty.concord.yaml");

        EdtTestUtil.runInEdtAndWait(() -> {
            myFixture.testStructureView(svc -> {
                var root = svc.getTreeModel().getRoot();
                assertNotNull(root);

//                printStructure(root, 0);

                var children = root.getChildren();
                assertNotNull(children);
                assertEquals(0, children.length);
            });
        });
    }

    private static TreeElement assertElement(TreeElement [] elements, String name) {
        var result = Arrays.stream(elements)
                .filter(e -> name.equals(e.getPresentation().getPresentableText()))
                .toList();

        if (result.isEmpty()) {
            throw new AssertionError("Element " + name + " not found");
        } else if (result.size() > 1) {
            throw new AssertionError("Multiple elements found");
        }
        return result.getFirst();
    }

    private static void printStructure(StructureViewTreeElement element, int indent) {
        var prefix = " ".repeat(indent * 2);
        var text = element.getPresentation().getPresentableText();

        System.out.println(prefix + (text != null ? text : "<null>"));

        for (var child : element.getChildren()) {
            printStructure((StructureViewTreeElement) child, indent + 1);
        }
    }
}
