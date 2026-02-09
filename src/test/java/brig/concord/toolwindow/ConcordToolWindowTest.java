package brig.concord.toolwindow;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.toolwindow.nodes.ConcordTreeNode;
import brig.concord.toolwindow.nodes.RootNode;
import brig.concord.toolwindow.nodes.ScopeNode;
import com.intellij.openapi.application.ReadAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

public class ConcordToolWindowTest extends ConcordYamlTestBaseJunit5 {

    @Test
    public void testTreeStructureWithSingleRoot() {
        myFixture.addFileToProject("concord.yaml", "");

        var rootNode = new RootNode(getProject());
        var children = ReadAction.compute(rootNode::getChildren);

        Assertions.assertEquals(1, children.size());
        Assertions.assertInstanceOf(ScopeNode.class, children.getFirst());
    }

    @Test
    public void testTreeStructureWithNestedRoots() {
        myFixture.addFileToProject("a/concord.yaml", "");
        myFixture.addFileToProject("b/concord.yaml", "");

        var rootNode = new RootNode(getProject());
        var children = ReadAction.compute(rootNode::getChildren);

        Assertions.assertEquals(2, children.size());

        var names = children.stream()
                .map(ConcordTreeNode::getDisplayName)
                .collect(Collectors.toList());

        var expected = List.of("a", "b");
        Assertions.assertEquals(expected, names);
    }

    @Test
    public void testSorting() {
        myFixture.addFileToProject("z/concord.yaml", "");
        myFixture.addFileToProject("a/concord.yaml", "");

        var rootNode = new RootNode(getProject());
        var children = ReadAction.compute(rootNode::getChildren);

        Assertions.assertEquals(2, children.size());
        Assertions.assertEquals("a", children.get(0).getDisplayName());
        Assertions.assertEquals("z", children.get(1).getDisplayName());
    }

    @Test
    public void testScopeNodePresentation() {
        myFixture.addFileToProject("demo/concord.yaml", "");

        var rootNode = new RootNode(getProject());
        var children = ReadAction.compute(rootNode::getChildren);
        Assertions.assertEquals(1, children.size());

        var node = (ScopeNode) children.getFirst();

        // Verify displayName property
        Assertions.assertEquals("demo", node.getDisplayName());
    }

    @Test
    public void testScopeNodeGetNavigatable() {
        myFixture.addFileToProject("nav/concord.yaml", "flows: {}");

        var rootNode = new RootNode(getProject());
        var children = ReadAction.compute(rootNode::getChildren);
        Assertions.assertEquals(1, children.size());

        var node = (ScopeNode) children.getFirst();
        children = ReadAction.compute(node::getChildren);
        Assertions.assertEquals(1, children.size());

        var concordYamlNode = children.getFirst();

        var navigatable = ReadAction.compute(concordYamlNode::getNavigatable);
        Assertions.assertNotNull(navigatable);
    }

    @Test
    public void testRootNodeGetNavigatableReturnsNull() {
        var rootNode = new RootNode(getProject());
        Assertions.assertNull(rootNode.getNavigatable());
    }
}
