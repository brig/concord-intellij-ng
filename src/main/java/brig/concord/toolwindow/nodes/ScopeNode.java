package brig.concord.toolwindow.nodes;

import brig.concord.psi.ConcordRoot;
import com.intellij.icons.AllIcons;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiManager;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScopeNode extends ConcordTreeNode {

    private final ConcordRoot concordRoot;
    private final String displayName;

    public ScopeNode(@NotNull SimpleNode parent, @NotNull ConcordRoot concordRoot) {
        super(parent);
        this.concordRoot = concordRoot;
        this.displayName = calculateDisplayName();
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        if (myProject == null) {
            return null;
        }
        var psiFile = PsiManager.getInstance(myProject).findFile(concordRoot.getRootFile());
        if (psiFile instanceof Navigatable navigatable && navigatable.canNavigate()) {
            return navigatable;
        }
        return null;
    }

    public @NotNull String getDisplayName() {
        return displayName;
    }

    private @NotNull String calculateDisplayName() {
        var rootFile = concordRoot.getRootFile();
        var scopeDir = rootFile.getParent();
        if (scopeDir == null) {
            return concordRoot.getScopeName();
        }

        if (myProject != null) {
            for (var contentRoot : ProjectRootManager.getInstance(myProject).getContentRoots()) {
                var relative = VfsUtilCore.getRelativePath(scopeDir, contentRoot);
                if (relative != null) {
                    return relative.isEmpty() ? "/" : relative;
                }
            }
        }

        return concordRoot.getScopeName();
    }

    @Override
    protected void update(@NotNull PresentationData data) {
        data.setIcon(AllIcons.Nodes.Folder);
        data.setPresentableText(displayName);
    }

    @Override
    public @NotNull ConcordTreeNode[] getChildren() {
        return new ConcordTreeNode[0];
    }
}
