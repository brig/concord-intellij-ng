// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.run.cli;

import brig.concord.ConcordBundle;
import brig.concord.ConcordNotifications;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public final class ConcordCliConfigurable implements Configurable {

    private TextFieldWithBrowseButton myCliPathField;
    private JBLabel myVersionLabel;

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return ConcordBundle.message("cli.settings.display.name");
    }

    @Override
    public @Nullable JComponent createComponent() {
        myCliPathField = new TextFieldWithBrowseButton();
        var descriptor = new FileChooserDescriptor(true, false, false, false, false, false)
                .withTitle(ConcordBundle.message("cli.settings.browse.title"));
        myCliPathField.addBrowseFolderListener(null, descriptor);
        myCliPathField.getTextField().getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                updateVersionLabel();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                updateVersionLabel();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                updateVersionLabel();
            }
        });

        myVersionLabel = new JBLabel();

        var downloadButton = new JButton(ConcordBundle.message("cli.settings.download.button"));
        downloadButton.addActionListener(e -> showDownloadDialog());

        var pathPanel = new JPanel(new BorderLayout(JBUI.scale(5), 0));
        pathPanel.add(myCliPathField, BorderLayout.CENTER);
        pathPanel.add(downloadButton, BorderLayout.EAST);

        return FormBuilder.createFormBuilder()
                .addLabeledComponent(ConcordBundle.message("cli.settings.path.label"), pathPanel)
                .addLabeledComponent(ConcordBundle.message("cli.settings.version.label"), myVersionLabel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private void updateVersionLabel() {
        var path = myCliPathField.getText();
        if (path.isBlank()) {
            myVersionLabel.setText(ConcordBundle.message("cli.settings.version.not.detected"));
            return;
        }

        var manager = ConcordCliManager.getInstance();
        if (manager.validateCliPath(path)) {
            var version = manager.detectCliVersion(path);
            myVersionLabel.setText(version != null ? version : ConcordBundle.message("cli.settings.version.not.detected"));
        } else {
            myVersionLabel.setText(ConcordBundle.message("cli.settings.invalid.path"));
        }
    }

    private void showDownloadDialog() {
        var dialog = new DownloadCliDialog();
        if (dialog.showAndGet()) {
            var selectedVersion = dialog.getSelectedVersion();
            if (selectedVersion != null) {
                downloadCli(selectedVersion);
            }
        }
    }

    private void downloadCli(@NotNull String version) {
        ProgressManager.getInstance().run(new Task.Modal(null, ConcordBundle.message("cli.settings.download.title"), true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    ConcordCliManager.getInstance().downloadCli(version, indicator);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onSuccess() {
                var settings = ConcordCliSettings.getInstance();
                var cliPath = settings.getCliPath();
                if (cliPath != null) {
                    myCliPathField.setText(cliPath);
                }
                updateVersionLabel();

                ConcordNotifications.getGroup()
                        .createNotification(
                                ConcordBundle.message("cli.settings.download.success", version),
                                NotificationType.INFORMATION)
                        .notify(null);
            }

            @Override
            public void onThrowable(@NotNull Throwable error) {
                ConcordNotifications.getGroup()
                        .createNotification(
                                ConcordBundle.message("cli.settings.download.error", error.getMessage()),
                                NotificationType.ERROR)
                        .notify(null);
            }
        });
    }

    @Override
    public boolean isModified() {
        var settings = ConcordCliSettings.getInstance();
        var currentPath = settings.getCliPath();
        var fieldPath = myCliPathField.getText();

        if (currentPath == null) {
            return !fieldPath.isBlank();
        }
        return !currentPath.equals(fieldPath);
    }

    @Override
    public void apply() throws ConfigurationException {
        var path = myCliPathField.getText();
        if (!path.isBlank()) {
            var manager = ConcordCliManager.getInstance();
            if (!manager.validateCliPath(path)) {
                throw new ConfigurationException(ConcordBundle.message("cli.settings.invalid.path"));
            }

            var version = manager.detectCliVersion(path);
            var settings = ConcordCliSettings.getInstance();
            settings.setCliPath(path);
            settings.setCliVersion(version);
        } else {
            var settings = ConcordCliSettings.getInstance();
            settings.setCliPath(null);
            settings.setCliVersion(null);
        }
    }

    @Override
    public void reset() {
        var settings = ConcordCliSettings.getInstance();
        var cliPath = settings.getCliPath();
        myCliPathField.setText(cliPath != null ? cliPath : "");
        updateVersionLabel();
    }

    private static class DownloadCliDialog extends DialogWrapper {

        private ComboBox<String> myVersionComboBox;
        private boolean myVersionsLoaded = false;

        protected DownloadCliDialog() {
            super(true);
            setTitle(ConcordBundle.message("cli.settings.download.title"));
            setResizable(false);
            init();
            loadVersions();
        }

        @Override
        protected @Nullable JComponent createCenterPanel() {
            myVersionComboBox = new ComboBox<>();
            myVersionComboBox.setEnabled(false);
            myVersionComboBox.setMinimumAndPreferredWidth(JBUI.scale(300));
            myVersionComboBox.addItem(ConcordBundle.message("cli.settings.fetching.versions"));

            return FormBuilder.createFormBuilder()
                    .addLabeledComponent(ConcordBundle.message("cli.settings.download.select.version"), myVersionComboBox)
                    .getPanel();
        }

        private void loadVersions() {
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                try {
                    var versions = ConcordCliManager.getInstance().fetchAvailableVersions()
                            .stream()
                            .limit(10)
                            .toList();

                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (!isShowing()) {
                            return;
                        }
                        myVersionComboBox.removeAllItems();
                        for (var version : versions) {
                            myVersionComboBox.addItem(version);
                        }
                        myVersionComboBox.setEnabled(true);
                        myVersionsLoaded = true;
                    }, ModalityState.stateForComponent(myVersionComboBox));
                } catch (IOException e) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (!isShowing()) {
                            return;
                        }
                        myVersionComboBox.removeAllItems();
                        myVersionComboBox.addItem(ConcordBundle.message("cli.settings.download.error", e.getMessage()));
                    }, ModalityState.stateForComponent(myVersionComboBox));
                }
            });
        }

        @Override
        protected void doOKAction() {
            if (myVersionsLoaded && myVersionComboBox.getSelectedItem() != null) {
                super.doOKAction();
            }
        }

        public @Nullable String getSelectedVersion() {
            var selected = myVersionComboBox.getSelectedItem();
            return selected != null ? selected.toString() : null;
        }
    }
}
