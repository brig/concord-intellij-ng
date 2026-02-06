package brig.concord.toolwindow.nodes;

import brig.concord.dependency.DependencyCollector.DependencyOccurrence;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * Node representing the dependencies section under a scope.
 */
public class DependenciesNode extends ConcordTreeNode {

    private final Project project;
    private final List<DependencyOccurrence> occurrences;

    public DependenciesNode(@NotNull Project project,
                            @NotNull List<DependencyOccurrence> occurrences) {
        this.project = project;
        this.occurrences = occurrences;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "Dependencies";
    }

    @Override
    public @Nullable Icon getIcon() {
        return AllIcons.Nodes.PpLibFolder;
    }

    @Override
    public @NotNull List<ConcordTreeNode> getChildren() {
        // Group occurrences by coordinate
        var grouped = new LinkedHashMap<String, List<DependencyOccurrence>>();
        for (var occ : occurrences) {
            grouped.computeIfAbsent(occ.coordinate().toGav(), k -> new ArrayList<>()).add(occ);
        }

        return grouped.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().toLowerCase()))
                .map(e -> (ConcordTreeNode) new DependencyNode(project, e.getValue()))
                .toList();
    }

    @Override
    public @Nullable Navigatable getNavigatable() {
        return null;
    }
}
