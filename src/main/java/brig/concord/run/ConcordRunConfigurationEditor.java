// SPDX-License-Identifier: Apache-2.0
package brig.concord.run;

import brig.concord.ConcordBundle;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ConcordRunConfigurationEditor extends SettingsEditor<ConcordRunConfiguration> {

    private final Project myProject;
    private JBLabel myRunModeLabel;
    private HyperlinkLabel myRunModeLink;
    private JBTextField myEntryPointField;
    private TextFieldWithBrowseButton myWorkingDirectoryField;
    private ParametersTablePanel myParametersPanel;
    private RawCommandLineEditor myAdditionalArgumentsField;

    public ConcordRunConfigurationEditor(@NotNull Project project) {
        myProject = project;
    }

    @Override
    protected void resetEditorFrom(@NotNull ConcordRunConfiguration configuration) {
        updateRunModeLabel();

        myEntryPointField.setText(configuration.getEntryPoint());
        myWorkingDirectoryField.setText(configuration.getWorkingDirectory());
        myAdditionalArgumentsField.setText(configuration.getAdditionalArguments());
        myParametersPanel.setParameters(configuration.getParameters());
    }

    private void updateRunModeLabel() {
        var settings = ConcordRunModeSettings.getInstance(myProject);
        var mode = settings.getRunMode();
        var modeText = mode == ConcordRunModeSettings.RunMode.DIRECT
                ? ConcordBundle.message("run.mode.direct.label")
                : ConcordBundle.message("run.mode.delegating.label");
        myRunModeLabel.setText(ConcordBundle.message("run.configuration.run.mode.hint", modeText));
        myRunModeLink.setHyperlinkText(ConcordBundle.message("run.configuration.run.mode.link"));
    }

    @Override
    protected void applyEditorTo(@NotNull ConcordRunConfiguration configuration) {
        configuration.setEntryPoint(myEntryPointField.getText().trim());
        configuration.setWorkingDirectory(myWorkingDirectoryField.getText().trim());
        configuration.setAdditionalArguments(myAdditionalArgumentsField.getText().trim());
        configuration.setParameters(myParametersPanel.getParameters());
    }

    @Override
    protected @NotNull JComponent createEditor() {
        myRunModeLabel = new JBLabel();
        myRunModeLabel.setForeground(UIUtil.getContextHelpForeground());

        myRunModeLink = new HyperlinkLabel();
        myRunModeLink.addHyperlinkListener(e -> {
            ShowSettingsUtil.getInstance().showSettingsDialog(
                    myProject,
                    ConcordRunModeConfigurable.class
            );
        });

        var runModePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        runModePanel.add(myRunModeLabel);
        runModePanel.add(Box.createHorizontalStrut(4));
        runModePanel.add(myRunModeLink);

        myEntryPointField = new JBTextField();

        myWorkingDirectoryField = new TextFieldWithBrowseButton();
        var workingDirDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        workingDirDescriptor.setTitle(ConcordBundle.message("run.configuration.working.dir.browse.title"));
        myWorkingDirectoryField.addBrowseFolderListener(myProject, workingDirDescriptor);

        myParametersPanel = new ParametersTablePanel("Name", "Value");

        myAdditionalArgumentsField = new RawCommandLineEditor();

        var parametersComponent = LabeledComponent.create(
                myParametersPanel.getPanel(),
                ConcordBundle.message("run.configuration.parameters.label")
        );
        parametersComponent.setLabelLocation(BorderLayout.NORTH);

        return FormBuilder.createFormBuilder()
                .addComponent(runModePanel)
                .addLabeledComponent(ConcordBundle.message("run.configuration.entry.point.label"), myEntryPointField)
                .addLabeledComponent(ConcordBundle.message("run.configuration.working.dir.label"), myWorkingDirectoryField)
                .addComponent(parametersComponent)
                .addLabeledComponent(ConcordBundle.message("run.configuration.additional.args.label"), myAdditionalArgumentsField)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }
}
