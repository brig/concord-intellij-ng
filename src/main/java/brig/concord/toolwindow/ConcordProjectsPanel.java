package brig.concord.toolwindow;

import brig.concord.psi.ConcordModificationTracker;
import brig.concord.psi.ConcordScopeListener;
import brig.concord.toolwindow.nodes.ConcordTreeNode;
import brig.concord.toolwindow.nodes.RootNode;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ConcordProjectsPanel extends JPanel implements Disposable {

    private final Project project;
    private final Tree tree;
    private final DefaultTreeModel treeModel;

    public ConcordProjectsPanel(@NotNull Project project) {
        super(new BorderLayout());
        this.project = project;

        var rootTreeNode = buildTreeModel();
        this.treeModel = new DefaultTreeModel(rootTreeNode);
        this.tree = new Tree(treeModel);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new ConcordTreeCellRenderer());

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    handleDoubleClick();
                }
            }
        });

        var scrollPane = new JBScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);

        // Add toolbar
        var toolbar = createToolbar();
        add(toolbar.getComponent(), BorderLayout.NORTH);

        // Subscribe to scope changes
        project.getMessageBus().connect(this)
                .subscribe(ConcordScopeListener.TOPIC, (ConcordScopeListener) () ->
                        ApplicationManager.getApplication().invokeLater(this::refresh));
    }

    private @NotNull ActionToolbar createToolbar() {
        var refreshAction = new AnAction("Refresh", "Refresh Concord scopes", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ConcordModificationTracker.getInstance(project).invalidate();
                refresh();
            }

            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };

        var actionGroup = new DefaultActionGroup(refreshAction);
        var toolbar = ActionManager.getInstance().createActionToolbar("ConcordToolWindow", actionGroup, true);
        toolbar.setTargetComponent(this);
        return toolbar;
    }

    @Override
    public void dispose() {
        // Connection is automatically disposed via connect(this)
    }

    public void refresh() {
        var rootTreeNode = buildTreeModel();
        treeModel.setRoot(rootTreeNode);
        tree.expandRow(0);
    }

    private @NotNull DefaultMutableTreeNode buildTreeModel() {
        var rootNode = new RootNode(project);
        var rootTreeNode = new DefaultMutableTreeNode(rootNode);

        for (var child : rootNode.getChildren()) {
            addNodeRecursively(rootTreeNode, child);
        }

        return rootTreeNode;
    }

    private void addNodeRecursively(@NotNull DefaultMutableTreeNode parent, @NotNull ConcordTreeNode node) {
        var treeNode = new DefaultMutableTreeNode(node);
        parent.add(treeNode);

        for (var child : node.getChildren()) {
            addNodeRecursively(treeNode, child);
        }
    }

    private void handleDoubleClick() {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath == null) {
            return;
        }

        var lastComponent = selectionPath.getLastPathComponent();
        if (!(lastComponent instanceof DefaultMutableTreeNode treeNode)) {
            return;
        }

        var userObject = treeNode.getUserObject();
        if (!(userObject instanceof ConcordTreeNode concordNode)) {
            return;
        }

        Navigatable navigatable = concordNode.getNavigatable();
        if (navigatable != null && navigatable.canNavigate()) {
            navigatable.navigate(true);
        }
    }

    private static class ConcordTreeCellRenderer extends ColoredTreeCellRenderer {
        @Override
        public void customizeCellRenderer(@NotNull JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
            if (!(value instanceof DefaultMutableTreeNode treeNode)) {
                return;
            }

            var userObject = treeNode.getUserObject();
            if (!(userObject instanceof ConcordTreeNode concordNode)) {
                return;
            }

            append(concordNode.getDisplayName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            setIcon(concordNode.getIcon());
        }
    }
}
