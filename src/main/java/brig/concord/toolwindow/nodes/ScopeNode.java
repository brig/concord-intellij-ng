package brig.concord.toolwindow.nodes;

import brig.concord.psi.ConcordRoot;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * Node representing a Concord project scope (root concord.yaml).
 */
public class ScopeNode extends ConcordTreeNode {

    private final Project project;
    private final ConcordRoot concordRoot;

    public ScopeNode(@NotNull Project project, @NotNull ConcordRoot concordRoot) {
        this.project = project;
        this.concordRoot = concordRoot;
    }

    @Override
    public @NotNull String getDisplayName() {
        var rootFile = concordRoot.getRootFile();
        var scopeDir = rootFile.getParent();
        if (scopeDir == null) {
            return concordRoot.getScopeName();
        }

        for (var contentRoot : ProjectRootManager.getInstance(project).getContentRoots()) {
            var relative = VfsUtilCore.getRelativePath(scopeDir, contentRoot);
            if (relative != null) {
                return relative.isEmpty() ? "/" : relative;
            }
        }

        return concordRoot.getScopeName();
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.Nodes.Folder;
    }

    @Override
    public @NotNull List<ConcordTreeNode> getChildren() {
        return List.of();
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        var psiFile = PsiManager.getInstance(project).findFile(concordRoot.getRootFile());
        if (psiFile instanceof Navigatable navigatable) {
            return navigatable;
        }
        return null;
    }
}
