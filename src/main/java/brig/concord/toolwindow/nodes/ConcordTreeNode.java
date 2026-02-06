package brig.concord.toolwindow.nodes;

import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ConcordTreeNode extends SimpleNode {

    protected ConcordTreeNode(Project project) {
        super(project);
    }

    protected ConcordTreeNode(SimpleNode parent) {
        super(parent);
    }

    public @Nullable Navigatable getNavigatable() {
        return null;
    }

    @Override
    public abstract @NotNull ConcordTreeNode[] getChildren();
}
