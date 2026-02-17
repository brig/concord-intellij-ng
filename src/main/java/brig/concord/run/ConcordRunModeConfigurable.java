// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.run;

import brig.concord.ConcordBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ConcordRunModeConfigurable implements Configurable {

    private final Project myProject;
    private ComboBox<RunModeItem> myRunModeComboBox;
    private JBTextField myMainEntryPointField;
    private JBTextField myFlowParameterNameField;
    private JBTextField myActiveProfilesField;
    private ParametersTablePanel myDefaultParametersPanel;
    private JBLabel myMainEntryPointLabel;
    private JBLabel myFlowParameterNameLabel;

    public ConcordRunModeConfigurable(@NotNull Project project) {
        myProject = project;
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return ConcordBundle.message("run.mode.settings.display.name");
    }

    @Override
    public @Nullable JComponent createComponent() {
        myRunModeComboBox = new ComboBox<>(new RunModeItem[]{
                new RunModeItem(ConcordRunModeSettings.RunMode.DIRECT,
                        ConcordBundle.message("run.mode.direct.label")),
                new RunModeItem(ConcordRunModeSettings.RunMode.DELEGATING,
                        ConcordBundle.message("run.mode.delegating.label"))
        });

        myMainEntryPointField = new JBTextField();
        myFlowParameterNameField = new JBTextField();
        myActiveProfilesField = new JBTextField();
        myActiveProfilesField.getEmptyText().setText(ConcordBundle.message("run.mode.active.profiles.placeholder"));

        myDefaultParametersPanel = new ParametersTablePanel(
                ConcordBundle.message("run.mode.default.params.column.name"),
                ConcordBundle.message("run.mode.default.params.column.value")
        );

        var parametersComponent = LabeledComponent.create(
                myDefaultParametersPanel.getPanel(),
                ConcordBundle.message("run.mode.default.params.label")
        );
        parametersComponent.setLabelLocation(BorderLayout.NORTH);

        myMainEntryPointLabel = new JBLabel(ConcordBundle.message("run.mode.main.entry.point.label"));
        myFlowParameterNameLabel = new JBLabel(ConcordBundle.message("run.mode.flow.param.name.label"));

        myRunModeComboBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                updateFieldsVisibility();
            }
        });

        return FormBuilder.createFormBuilder()
                .addLabeledComponent(ConcordBundle.message("run.mode.label"), myRunModeComboBox)
                .addLabeledComponent(myMainEntryPointLabel, myMainEntryPointField)
                .addLabeledComponent(myFlowParameterNameLabel, myFlowParameterNameField)
                .addLabeledComponent(ConcordBundle.message("run.mode.active.profiles.label"), myActiveProfilesField)
                .addComponent(parametersComponent)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private void updateFieldsVisibility() {
        var selectedItem = myRunModeComboBox.getSelectedItem();
        boolean isDelegating = selectedItem instanceof RunModeItem item
                && item.mode() == ConcordRunModeSettings.RunMode.DELEGATING;

        myMainEntryPointLabel.setVisible(isDelegating);
        myMainEntryPointField.setVisible(isDelegating);
        myFlowParameterNameLabel.setVisible(isDelegating);
        myFlowParameterNameField.setVisible(isDelegating);
    }

    @Override
    public boolean isModified() {
        var settings = ConcordRunModeSettings.getInstance(myProject);

        var selectedItem = myRunModeComboBox.getSelectedItem();
        if (selectedItem instanceof RunModeItem item && item.mode() != settings.getRunMode()) {
            return true;
        }

        if (!myMainEntryPointField.getText().trim().equals(settings.getMainEntryPoint())) {
            return true;
        }

        if (!myFlowParameterNameField.getText().trim().equals(settings.getFlowParameterName())) {
            return true;
        }

        if (!parseProfiles(myActiveProfilesField.getText()).equals(settings.getActiveProfiles())) {
            return true;
        }

        return !myDefaultParametersPanel.getParameters().equals(settings.getDefaultParameters());
    }

    private static @NotNull List<String> parseProfiles(@NotNull String text) {
        if (text.isBlank()) {
            return List.of();
        }
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private static @NotNull String formatProfiles(@NotNull List<String> profiles) {
        return String.join(", ", profiles);
    }

    @Override
    public void apply() throws ConfigurationException {
        var selectedItem = myRunModeComboBox.getSelectedItem();
        if (!(selectedItem instanceof RunModeItem item)) {
            return;
        }

        if (item.mode() == ConcordRunModeSettings.RunMode.DELEGATING) {
            var mainEntryPoint = myMainEntryPointField.getText();
            if (mainEntryPoint.isBlank()) {
                throw new ConfigurationException(ConcordBundle.message("run.mode.main.entry.point.empty"));
            }

            var flowParamName = myFlowParameterNameField.getText();
            if (flowParamName.isBlank()) {
                throw new ConfigurationException(ConcordBundle.message("run.mode.flow.param.name.empty"));
            }
        }

        var settings = ConcordRunModeSettings.getInstance(myProject);
        settings.setRunMode(item.mode());
        settings.setMainEntryPoint(myMainEntryPointField.getText().trim());
        settings.setFlowParameterName(myFlowParameterNameField.getText().trim());
        settings.setActiveProfiles(parseProfiles(myActiveProfilesField.getText()));
        settings.setDefaultParameters(myDefaultParametersPanel.getParameters());
    }

    @Override
    public void reset() {
        var settings = ConcordRunModeSettings.getInstance(myProject);

        // Select the correct run mode
        for (int i = 0; i < myRunModeComboBox.getItemCount(); i++) {
            var item = myRunModeComboBox.getItemAt(i);
            if (item.mode() == settings.getRunMode()) {
                myRunModeComboBox.setSelectedIndex(i);
                break;
            }
        }

        myMainEntryPointField.setText(settings.getMainEntryPoint());
        myFlowParameterNameField.setText(settings.getFlowParameterName());
        myActiveProfilesField.setText(formatProfiles(settings.getActiveProfiles()));

        myDefaultParametersPanel.setParameters(settings.getDefaultParameters());

        updateFieldsVisibility();
    }

    private record RunModeItem(@NotNull ConcordRunModeSettings.RunMode mode, @NotNull String label) {
        @Override
        public String toString() {
            return label;
        }
    }
}
