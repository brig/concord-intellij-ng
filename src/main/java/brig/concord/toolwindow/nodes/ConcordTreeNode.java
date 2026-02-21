// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.toolwindow.nodes;

import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public abstract class ConcordTreeNode {

    /**
     * A navigation target with a label for popup display.
     */
    public record NavigationTarget(@NotNull String label, @NotNull Navigatable navigatable) {}

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
     * Returns navigation targets for this node.
     * Most nodes have 0 or 1 target (derived from getNavigatable).
     * Override for nodes with multiple targets (e.g. DependencyNode).
     */
    public @NotNull List<NavigationTarget> getNavigationTargets() {
        var nav = getNavigatable();
        if (nav == null) {
            return List.of();
        }
        return List.of(new NavigationTarget(getDisplayName(), nav));
    }

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
