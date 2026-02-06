package brig.concord.toolwindow;

import brig.concord.psi.ConcordProjectListener;
import brig.concord.toolwindow.nodes.ConcordTreeNode;
import com.intellij.ide.util.treeView.NodeRenderer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class ConcordProjectsPanel extends JPanel implements Disposable {

    private final StructureTreeModel<ConcordTreeStructure> structureTreeModel;

    public ConcordProjectsPanel(@NotNull Project project) {
        super(new BorderLayout());

        var structure = new ConcordTreeStructure(project);
        this.structureTreeModel = new StructureTreeModel<>(structure, this);
        var asyncTreeModel = new AsyncTreeModel(structureTreeModel, this);
        var tree = new Tree(asyncTreeModel);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setCellRenderer(new NodeRenderer());

        new DoubleClickListener() {
            @Override
            protected boolean onDoubleClick(@NotNull MouseEvent event) {
                return handleDoubleClick(tree);
            }
        }.installOn(tree);

        add(new JBScrollPane(tree), BorderLayout.CENTER);

        var toolbar = createToolbar();
        add(toolbar.getComponent(), BorderLayout.NORTH);

        project.getMessageBus().connect(this)
                .subscribe(ConcordProjectListener.TOPIC, (ConcordProjectListener) this::refresh);
    }

    private @NotNull ActionToolbar createToolbar() {
        var actionGroup = new DefaultActionGroup(new RefreshConcordScopesAction());
        var toolbar = ActionManager.getInstance().createActionToolbar("ConcordToolWindow", actionGroup, true);
        toolbar.setTargetComponent(this);
        return toolbar;
    }

    @Override
    public void dispose() {
    }

    private void refresh() {
        structureTreeModel.invalidateAsync();
    }

    private static boolean handleDoubleClick(@NotNull Tree tree) {
        var selectionPath = tree.getSelectionPath();
        if (selectionPath == null) {
            return false;
        }

        var node = TreeUtil.getLastUserObject(ConcordTreeNode.class, selectionPath);
        if (node == null) {
            return false;
        }

        var navigatable = node.getNavigatable();
        if (navigatable != null) {
            navigatable.navigate(true);
            return true;
        }
        return false;
    }
}
