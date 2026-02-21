// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.toolwindow.nodes;

import brig.concord.ConcordIcons;
import brig.concord.dependency.DependencyCollector.DependencyOccurrence;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Node representing a Maven dependency from configuration.dependencies.
 */
public class DependencyNode extends ConcordTreeNode {

    private final Project project;
    private final List<DependencyOccurrence> occurrences;

    public DependencyNode(@NotNull Project project,
                          @NotNull List<DependencyOccurrence> occurrences) {
        if (occurrences.isEmpty()) {
            throw new IllegalArgumentException("occurrences must not be empty");
        }
        this.project = project;
        this.occurrences = occurrences;
    }

    @Override
    public @NotNull String getDisplayName() {
        return occurrences.get(0).coordinate().toGav();
    }

    @Override
    public @Nullable Icon getIcon() {
        return ConcordIcons.MAVEN_PROJECT;
    }

    @Override
    public @NotNull List<ConcordTreeNode> getChildren() {
        return List.of();
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        if (occurrences.size() == 1) {
            var occ = occurrences.get(0);
            return new OpenFileDescriptor(project, occ.file(), occ.textOffset());
        }
        return null;
    }

    @Override
    public @NotNull List<NavigationTarget> getNavigationTargets() {
        var targets = new ArrayList<NavigationTarget>();
        for (var occ : occurrences) {
            var nav = new OpenFileDescriptor(project, occ.file(), occ.textOffset());
            targets.add(new NavigationTarget(computeLabel(occ), nav));
        }
        return targets;
    }

    private @NotNull String computeLabel(@NotNull DependencyOccurrence occ) {
        for (var contentRoot : ProjectRootManager.getInstance(project).getContentRoots()) {
            var relative = VfsUtilCore.getRelativePath(occ.file(), contentRoot);
            if (relative != null) {
                return relative;
            }
        }
        return occ.file().getName();
    }
}
