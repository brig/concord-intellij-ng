package brig.concord.toolwindow.nodes;

import brig.concord.psi.ConcordRoot;
import brig.concord.psi.ConcordScopeService;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

/**
 * Invisible root node that holds all Concord scope nodes.
 */
public class RootNode extends ConcordTreeNode {

    public RootNode(@NotNull Project project) {
        super(project);
    }

    @Override
    public @NotNull ConcordTreeNode[] getChildren() {
        if (myProject == null || myProject.isDisposed()) {
            return new ConcordTreeNode[0];
        }
        
        List<ConcordRoot> roots = ConcordScopeService.getInstance(myProject).findRoots();
        return roots.stream()
                .map(root -> new ScopeNode(this, root))
                .sorted(Comparator.comparing(ScopeNode::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toArray(ConcordTreeNode[]::new);
    }

    @Override
    protected void update(@NotNull PresentationData data) {
        // Root is invisible, but good to have a name for debugging
        data.setPresentableText("Concord");
    }
}
