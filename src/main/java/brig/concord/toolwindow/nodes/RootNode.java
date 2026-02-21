// SPDX-License-Identifier: Apache-2.0
package brig.concord.toolwindow.nodes;

import brig.concord.dependency.DependencyCollector;
import brig.concord.psi.ConcordRoot;
import brig.concord.psi.ConcordScopeService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        var collector = DependencyCollector.getInstance(project);
        var scopeService = ConcordScopeService.getInstance(project);

        return collector.collectByScope().stream()
                .map(sd -> {
                    var resourceFiles = computeResourceFiles(scopeService, sd.root());
                    return (ConcordTreeNode) new ScopeNode(project, sd.root(), sd.occurrences(), resourceFiles);
                })
                .sorted(Comparator.comparing(ConcordTreeNode::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private @NotNull Set<VirtualFile> computeResourceFiles(@NotNull ConcordScopeService scopeService,
                                                           @NotNull ConcordRoot root) {
        var allFiles = scopeService.getFilesInScope(root);
        var rootFile = root.getRootFile();
        var result = new HashSet<>(allFiles);
        result.remove(rootFile);
        return result;
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        return null;
    }
}
