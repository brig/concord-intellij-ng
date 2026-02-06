package brig.concord.toolwindow;

import brig.concord.ConcordYamlTestBase;
import brig.concord.toolwindow.nodes.ConcordTreeNode;
import brig.concord.toolwindow.nodes.RootNode;
import brig.concord.toolwindow.nodes.ScopeNode;
import com.intellij.openapi.application.ReadAction;
import com.intellij.pom.Navigatable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConcordToolWindowTest extends ConcordYamlTestBase {

    @Test
    public void testTreeStructureWithSingleRoot() {
        myFixture.addFileToProject("concord.yaml", "");

        RootNode rootNode = new RootNode(getProject());
        ConcordTreeNode[] children = ReadAction.compute(rootNode::getChildren);

        assertEquals(1, children.length);
        assertInstanceOf(children[0], ScopeNode.class);
    }

    @Test
    public void testTreeStructureWithNestedRoots() {
        myFixture.addFileToProject("a/concord.yaml", "");
        myFixture.addFileToProject("b/concord.yaml", "");

        RootNode rootNode = new RootNode(getProject());
        ConcordTreeNode[] children = ReadAction.compute(rootNode::getChildren);

        assertEquals(2, children.length);

        var names = Arrays.stream(children)
                .map(node -> ((ScopeNode) node).getDisplayName())
                .collect(Collectors.toList());

        List<String> expected = List.of("a", "b");
        assertEquals(expected, names);
    }

    @Test
    public void testSorting() {
        myFixture.addFileToProject("z/concord.yaml", "");
        myFixture.addFileToProject("a/concord.yaml", "");

        RootNode rootNode = new RootNode(getProject());
        ConcordTreeNode[] children = ReadAction.compute(rootNode::getChildren);

        assertEquals(2, children.length);
        assertEquals("a", ((ScopeNode) children[0]).getDisplayName());
        assertEquals("z", ((ScopeNode) children[1]).getDisplayName());
    }

    @Test
    public void testScopeNodePresentation() {
        myFixture.addFileToProject("demo/concord.yaml", "");

        RootNode rootNode = new RootNode(getProject());
        ConcordTreeNode[] children = ReadAction.compute(rootNode::getChildren);
        assertEquals(1, children.length);

        ScopeNode node = (ScopeNode) children[0];

        // Verify displayName property
        assertEquals("demo", node.getDisplayName());

        // Verify SimpleNode presentation update
        node.update();
        assertEquals("demo", node.getPresentation().getPresentableText());
    }

    @Test
    public void testScopeNodeGetNavigatable() {
        myFixture.addFileToProject("nav/concord.yaml", "flows: {}");

        RootNode rootNode = new RootNode(getProject());
        ConcordTreeNode[] children = ReadAction.compute(rootNode::getChildren);
        assertEquals(1, children.length);

        ScopeNode node = (ScopeNode) children[0];
        Navigatable navigatable = ReadAction.compute(node::getNavigatable);
        assertNotNull(navigatable);
    }

    @Test
    public void testRootNodeGetNavigatableReturnsNull() {
        RootNode rootNode = new RootNode(getProject());
        assertNull(rootNode.getNavigatable());
    }
}
