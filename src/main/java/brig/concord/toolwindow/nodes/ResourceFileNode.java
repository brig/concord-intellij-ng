// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.toolwindow.nodes;

import brig.concord.ConcordIcons;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * Node representing a resource file within a scope (not the root file).
 */
public class ResourceFileNode extends ConcordTreeNode {

    private final Project project;
    private final VirtualFile file;
    private final VirtualFile rootDir;

    public ResourceFileNode(@NotNull Project project,
                            @NotNull VirtualFile file,
                            @NotNull VirtualFile rootDir) {
        this.project = project;
        this.file = file;
        this.rootDir = rootDir;
    }

    @Override
    public @NotNull String getDisplayName() {
        var relative = VfsUtilCore.getRelativePath(file, rootDir);
        return relative != null ? relative : file.getName();
    }

    @Override
    public @Nullable Icon getIcon() {
        return ConcordIcons.FILE;
    }

    @Override
    public @NotNull List<ConcordTreeNode> getChildren() {
        return List.of();
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        return new OpenFileDescriptor(project, file);
    }
}
