package brig.concord.run;

import brig.concord.ConcordBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.table.JBTable;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConcordRunConfigurationEditor extends SettingsEditor<ConcordRunConfiguration> {

    private final Project myProject;
    private JBTextField myEntryPointField;
    private TextFieldWithBrowseButton myWorkingDirectoryField;
    private DefaultTableModel myParametersTableModel;
    private RawCommandLineEditor myAdditionalArgumentsField;

    public ConcordRunConfigurationEditor(@NotNull Project project) {
        myProject = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull ConcordRunConfiguration configuration) {
        myEntryPointField.setText(configuration.getEntryPoint());
        myWorkingDirectoryField.setText(configuration.getWorkingDirectory());
        myAdditionalArgumentsField.setText(configuration.getAdditionalArguments());

        myParametersTableModel.setRowCount(0);
        var parameters = configuration.getParameters();
        for (var entry : parameters.entrySet()) {
            myParametersTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        myParametersTableModel.addRow(new Object[]{"", ""});
    }

    @Override
    protected void applyEditorTo(@NotNull ConcordRunConfiguration configuration) {
        configuration.setEntryPoint(myEntryPointField.getText().trim());
        configuration.setWorkingDirectory(myWorkingDirectoryField.getText().trim());
        configuration.setAdditionalArguments(myAdditionalArgumentsField.getText().trim());

        Map<String, String> parameters = new LinkedHashMap<>();
        for (var i = 0; i < myParametersTableModel.getRowCount(); i++) {
            var key = (String) myParametersTableModel.getValueAt(i, 0);
            var value = (String) myParametersTableModel.getValueAt(i, 1);
            if (key != null && !key.isBlank()) {
                parameters.put(key.trim(), value != null ? value.trim() : "");
            }
        }
        configuration.setParameters(parameters);
    }

    @Override
    protected @NotNull JComponent createEditor() {
        myEntryPointField = new JBTextField();

        myWorkingDirectoryField = new TextFieldWithBrowseButton();
        var workingDirDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        workingDirDescriptor.setTitle("Select Working Directory");
        myWorkingDirectoryField.addBrowseFolderListener(myProject, workingDirDescriptor);

        myParametersTableModel = new DefaultTableModel(new Object[]{"Name", "Value"}, 0);
        var myParametersTable = new JBTable(myParametersTableModel);
        myParametersTable.setShowGrid(true);
        myParametersTable.getTableHeader().setReorderingAllowed(false);

        myParametersTableModel.addTableModelListener(e -> {
            var lastRow = myParametersTableModel.getRowCount() - 1;
            if (lastRow >= 0) {
                var key = (String) myParametersTableModel.getValueAt(lastRow, 0);
                var value = (String) myParametersTableModel.getValueAt(lastRow, 1);
                if ((key != null && !key.isBlank()) || (value != null && !value.isBlank())) {
                    myParametersTableModel.addRow(new Object[]{"", ""});
                }
            }
        });

        var parametersScrollPane = new JBScrollPane(myParametersTable);
        parametersScrollPane.setPreferredSize(JBUI.size(-1, 120));

        myAdditionalArgumentsField = new RawCommandLineEditor();

        var parametersComponent = LabeledComponent.create(
                parametersScrollPane,
                ConcordBundle.message("run.configuration.parameters.label")
        );
        parametersComponent.setLabelLocation(BorderLayout.NORTH);

        return FormBuilder.createFormBuilder()
                .addLabeledComponent(ConcordBundle.message("run.configuration.entry.point.label"), myEntryPointField)
                .addLabeledComponent(ConcordBundle.message("run.configuration.working.dir.label"), myWorkingDirectoryField)
                .addComponent(parametersComponent)
                .addLabeledComponent(ConcordBundle.message("run.configuration.additional.args.label"), myAdditionalArgumentsField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }
}
