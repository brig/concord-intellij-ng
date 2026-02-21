// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.toolwindow;

import brig.concord.psi.ConcordDependenciesListener;
import brig.concord.psi.ConcordModificationTracker;
import brig.concord.psi.ConcordProjectListener;
import brig.concord.toolwindow.nodes.ConcordTreeNode;
import brig.concord.toolwindow.nodes.ConcordTreeNode.NavigationTarget;
import brig.concord.toolwindow.nodes.RootNode;
import com.intellij.ide.util.treeView.TreeState;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConcordProjectsPanel extends JPanel implements Disposable {

    private static final Logger LOG = Logger.getInstance(ConcordProjectsPanel.class);

    private final Project project;
    private final Tree tree;
    private final DefaultTreeModel treeModel;
    private final AtomicBoolean refreshPending = new AtomicBoolean(false);
    private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);

    public ConcordProjectsPanel(@NotNull Project project) {
        super(new BorderLayout());
        this.project = project;

        this.treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Loading..."));
        this.tree = new Tree(treeModel);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new ConcordTreeCellRenderer());

        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(@NotNull MouseEvent event) {
                return handleDoubleClick();
            }
        }.installOn(tree);

        var scrollPane = new JBScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);

        // Add toolbar
        var toolbar = createToolbar();
        add(toolbar.getComponent(), BorderLayout.NORTH);

        // Subscribe to scope and dependency changes
        var connection = project.getMessageBus().connect(this);
        connection.subscribe(ConcordProjectListener.TOPIC, (ConcordProjectListener) this::refresh);
        connection.subscribe(ConcordDependenciesListener.TOPIC, (ConcordDependenciesListener) this::refresh);

        // Initial async load
        refresh();
    }

    private @NotNull ActionToolbar createToolbar() {
        var refreshAction = new AnAction("Refresh", "Refresh Concord scopes", AllIcons.Actions.Refresh) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                ConcordModificationTracker.getInstance(project).forceRefresh();
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
        refreshPending.set(true);

        if (!refreshInProgress.compareAndSet(false, true)) {
            // A refresh is already running; it will pick up the pending flag
            return;
        }

        doRefresh();
    }

    private void doRefresh() {
        refreshPending.set(false);

        ReadAction.nonBlocking(() -> {
                    try {
                        return buildTreeData(new RootNode(project));
                    } catch (ProcessCanceledException e) {
                        throw e;
                    } catch (Exception e) {
                        LOG.warn("Failed to build Concord tool window tree", e);
                        return null;
                    }
                })
                .expireWith(this)
                .finishOnUiThread(ModalityState.defaultModalityState(), result -> {
                    if (result != null) {
                        applyTreeData(result);
                    } else {
                        scheduleNextOrComplete();
                    }
                })
                .submit(AppExecutorUtil.getAppExecutorService());
    }

    private void applyTreeData(@NotNull List<TreeNodeData> children) {
        TreeState state = TreeState.createOn(tree, true, true);

        var root = new DefaultMutableTreeNode("root");
        for (var child : children) {
            addDataNodeRecursively(root, child);
        }
        treeModel.setRoot(root);
        tree.expandRow(0);
        state.applyTo(tree);

        scheduleNextOrComplete();
    }

    private void scheduleNextOrComplete() {
        if (refreshPending.get()) {
            doRefresh();
        } else {
            refreshInProgress.set(false);
            // Double-check: a refresh could have been requested between the check and the set
            if (refreshPending.get() && refreshInProgress.compareAndSet(false, true)) {
                doRefresh();
            }
        }
    }

    private @NotNull List<TreeNodeData> buildTreeData(@NotNull ConcordTreeNode node) {
        var result = new ArrayList<TreeNodeData>();
        for (var child : node.getChildren()) {
            result.add(buildTreeDataRecursively(child));
        }
        return result;
    }

    private @NotNull TreeNodeData buildTreeDataRecursively(@NotNull ConcordTreeNode node) {
        var children = new ArrayList<TreeNodeData>();
        for (var child : node.getChildren()) {
            children.add(buildTreeDataRecursively(child));
        }
        return new TreeNodeData(
                node.getDisplayName(),
                node.getIcon(),
                node.getNavigationTargets(),
                children
        );
    }

    private void addDataNodeRecursively(@NotNull DefaultMutableTreeNode parent, @NotNull TreeNodeData data) {
        var treeNode = new DefaultMutableTreeNode(data);
        parent.add(treeNode);
        for (var child : data.children()) {
            addDataNodeRecursively(treeNode, child);
        }
    }

    private boolean handleDoubleClick() {
        TreePath selectionPath = tree.getSelectionPath();
        if (selectionPath == null) {
            return false;
        }

        var lastComponent = selectionPath.getLastPathComponent();
        if (!(lastComponent instanceof DefaultMutableTreeNode treeNode)) {
            return false;
        }

        var userObject = treeNode.getUserObject();
        if (!(userObject instanceof TreeNodeData data)) {
            return false;
        }

        var targets = data.navigationTargets();
        if (targets.size() == 1) {
            var nav = targets.get(0).navigatable();
            if (nav.canNavigate()) {
                nav.navigate(true);
                return true;
            }
        } else if (targets.size() > 1) {
            JBPopupFactory.getInstance()
                    .createPopupChooserBuilder(targets)
                    .setTitle("Navigate To")
                    .setItemChosenCallback(target -> {
                        if (target.navigatable().canNavigate()) {
                            target.navigatable().navigate(true);
                        }
                    })
                    .setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                                      int index, boolean isSelected,
                                                                      boolean cellHasFocus) {
                            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                            if (value instanceof NavigationTarget nt) {
                                setText(nt.label());
                            }
                            return this;
                        }
                    })
                    .createPopup()
                    .showInFocusCenter();
            return true;
        }
        return false;
    }

    record TreeNodeData(
            @NotNull String displayName,
            @Nullable Icon icon,
            @NotNull List<NavigationTarget> navigationTargets,
            @NotNull List<TreeNodeData> children
    ) {
        // Used by TreeState to identify nodes across tree rebuilds (preserve expansion/selection).
        // Default record toString() includes unstable fields (icon, children) — keep this stable.
        @Override
        public String toString() {
            return displayName;
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
            if (!(userObject instanceof TreeNodeData data)) {
                return;
            }

            append(data.displayName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            setIcon(data.icon());
        }
    }
}
