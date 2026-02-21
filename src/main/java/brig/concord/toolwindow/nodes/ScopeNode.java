// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.toolwindow.nodes;

import brig.concord.dependency.DependencyCollector.DependencyOccurrence;
import brig.concord.psi.ConcordRoot;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Node representing a Concord project scope (directory containing root concord.yaml).
 */
public class ScopeNode extends ConcordTreeNode {

    private final Project project;
    private final ConcordRoot concordRoot;
    private final List<DependencyOccurrence> depOccurrences;
    private final Set<VirtualFile> resourceFiles;
    private final String displayName;

    public ScopeNode(@NotNull Project project,
                     @NotNull ConcordRoot concordRoot,
                     @NotNull List<DependencyOccurrence> depOccurrences,
                     @NotNull Set<VirtualFile> resourceFiles) {
        this.project = project;
        this.concordRoot = concordRoot;
        this.depOccurrences = depOccurrences;
        this.resourceFiles = resourceFiles;
        this.displayName = computeDisplayName();
    }

    @Override
    public @NotNull String getDisplayName() {
        return displayName;
    }

    private @NotNull String computeDisplayName() {
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
        var children = new ArrayList<ConcordTreeNode>();

        // Always show the root file
        children.add(new RootFileNode(project, concordRoot.getRootFile()));

        // Dependencies container (only if non-empty)
        if (!depOccurrences.isEmpty()) {
            children.add(new DependenciesNode(project, depOccurrences));
        }

        // Resources container (only if non-empty)
        if (!resourceFiles.isEmpty()) {
            var rootDir = concordRoot.getRootFile().getParent();
            if (rootDir != null) {
                children.add(new ResourcesNode(project, rootDir, resourceFiles));
            }
        }

        return children;
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        return null;
    }
}
