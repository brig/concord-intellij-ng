package brig.concord.toolwindow.nodes;

import brig.concord.psi.ConcordRoot;
import brig.concord.psi.ConcordScopeService;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;

/**
 * Invisible root node that holds all Concord scope nodes.
 */
public class RootNode extends ConcordTreeNode {

    private final Project project;

    public RootNode(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Concord";
    }

    @Override
    public @Nullable Icon getIcon() {
        return null;
    }

    @Override
    public @NotNull List<ConcordTreeNode> getChildren() {
        List<ConcordRoot> roots = ConcordScopeService.getInstance(project).findRoots();
        return roots.stream()
                .map(root -> (ConcordTreeNode) new ScopeNode(project, root))
                .sorted(Comparator.comparing(ConcordTreeNode::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        return null;
    }
}
