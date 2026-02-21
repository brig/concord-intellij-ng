// SPDX-License-Identifier: Apache-2.0
package brig.concord.run;

import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ParametersTablePanel {

    private static final JBColor DUPLICATE_BACKGROUND = new JBColor(
            new Color(255, 235, 235),
            new Color(100, 50, 50)
    );

    private final DefaultTableModel myTableModel;
    private final JBTable myTable;
    private final JPanel myPanel;

    public ParametersTablePanel(@NotNull String nameColumnTitle, @NotNull String valueColumnTitle) {
        myTableModel = new DefaultTableModel(new Object[]{nameColumnTitle, valueColumnTitle}, 0);

        myTable = new JBTable(myTableModel);
        myTable.setShowGrid(true);
        myTable.getTableHeader().setReorderingAllowed(false);
        myTable.getColumnModel().getColumn(0).setCellRenderer(new DuplicateHighlightRenderer());

        myPanel = ToolbarDecorator.createDecorator(myTable)
                .setAddAction(button -> myTableModel.addRow(new Object[]{"", ""}))
                .setRemoveAction(button -> {
                    var selectedRow = myTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        myTableModel.removeRow(selectedRow);
                    }
                })
                .setPreferredSize(JBUI.size(-1, 150))
                .createPanel();
    }

    public @NotNull JPanel getPanel() {
        return myPanel;
    }

    public @NotNull Map<String, String> getParameters() {
        var parameters = new LinkedHashMap<String, String>();
        for (var i = 0; i < myTableModel.getRowCount(); i++) {
            var keyObj = myTableModel.getValueAt(i, 0);
            var valueObj = myTableModel.getValueAt(i, 1);
            var key = keyObj instanceof String s ? s : null;
            var value = valueObj instanceof String s ? s : null;
            if (key != null && !key.isBlank()) {
                parameters.put(key.trim(), value != null ? value.trim() : "");
            }
        }
        return parameters;
    }

    public void setParameters(@NotNull Map<String, String> parameters) {
        myTableModel.setRowCount(0);
        for (var entry : parameters.entrySet()) {
            myTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    private class DuplicateHighlightRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            var component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (!isSelected && isDuplicateKey(row)) {
                component.setBackground(DUPLICATE_BACKGROUND);
                setToolTipText("Duplicate parameter name");
            } else if (!isSelected) {
                component.setBackground(table.getBackground());
                setToolTipText(null);
            }

            return component;
        }

        private boolean isDuplicateKey(int currentRow) {
            var currentKeyObj = myTableModel.getValueAt(currentRow, 0);
            if (!(currentKeyObj instanceof String currentKey) || currentKey.isBlank()) {
                return false;
            }

            var trimmedKey = currentKey.trim();
            var count = 0;
            for (var i = 0; i < myTableModel.getRowCount(); i++) {
                var keyObj = myTableModel.getValueAt(i, 0);
                if (keyObj instanceof String key && !key.isBlank() && key.trim().equals(trimmedKey)) {
                    count++;
                    if (count > 1) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
