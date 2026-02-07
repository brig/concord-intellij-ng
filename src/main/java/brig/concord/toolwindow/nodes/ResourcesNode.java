package brig.concord.toolwindow.nodes;

import brig.concord.ConcordIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Node representing the "Resources" container under a scope.
 */
public class ResourcesNode extends ConcordTreeNode {

    private final Project project;
    private final VirtualFile rootDir;
    private final Set<VirtualFile> resourceFiles;

    public ResourcesNode(@NotNull Project project,
                          @NotNull VirtualFile rootDir,
                          @NotNull Set<VirtualFile> resourceFiles) {
        this.project = project;
        this.rootDir = rootDir;
        this.resourceFiles = resourceFiles;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Resources";
    }

    @Override
    public @Nullable Icon getIcon() {
        return ConcordIcons.RESOURCES;
    }

    @Override
    public @NotNull List<ConcordTreeNode> getChildren() {
        return resourceFiles.stream()
                .sorted(Comparator.comparing(VirtualFile::getPath))
                .map(f -> (ConcordTreeNode) new ResourceFileNode(project, f, rootDir))
                .toList();
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        return null;
    }
}
