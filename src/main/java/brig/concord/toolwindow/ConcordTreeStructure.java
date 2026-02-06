package brig.concord.toolwindow;

import brig.concord.toolwindow.nodes.RootNode;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import org.jetbrains.annotations.NotNull;

public class ConcordTreeStructure extends SimpleTreeStructure {

    private final RootNode rootNode;

    public ConcordTreeStructure(@NotNull Project project) {
        this.rootNode = new RootNode(project);
    }

    @Override
    public @NotNull Object getRootElement() {
        return rootNode;
    }
}
