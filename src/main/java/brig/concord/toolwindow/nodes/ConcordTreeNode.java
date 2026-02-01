package brig.concord.toolwindow.nodes;

import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public abstract class ConcordTreeNode {

    /**
     * Returns the display name shown in the tree.
     */
    public abstract @NotNull String getDisplayName();

    /**
     * Returns the icon for this node, or null for no icon.
     */
    public abstract @Nullable Icon getIcon();

    /**
     * Returns the children of this node.
     */
    public abstract @NotNull List<ConcordTreeNode> getChildren();

    /**
     * Returns a navigatable to jump to when double-clicked, or null if not navigable.
     */
    public abstract @Nullable Navigatable getNavigatable();

    /**
     * Returns true if this node has no children.
     */
    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
