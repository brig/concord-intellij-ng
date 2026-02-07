package brig.concord.toolwindow.nodes;

import brig.concord.ConcordIcons;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * Node representing the root concord.yml file within a scope.
 */
public class RootFileNode extends ConcordTreeNode {

    private final Project project;
    private final VirtualFile file;

    public RootFileNode(@NotNull Project project, @NotNull VirtualFile file) {
        this.project = project;
        this.file = file;
    }

    @Override
    public @NotNull String getDisplayName() {
        return file.getName();
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
