// SPDX-License-Identifier: Apache-2.0
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
import com.intellij.openapi.projectRoots.JavaSdkType;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Future;

public final class ConcordCliConfigurable implements Configurable {

    private TextFieldWithBrowseButton myCliPathField;
    private JBLabel myVersionLabel;
    private ComboBox<String> myJdkComboBox;
    private boolean myListenersActive;
    private int myVersionDetectionGeneration;
    private @Nullable Future<?> myVersionDetectionFuture;
    private @Nullable String myDetectedVersion;
    private @Nullable String myNotFoundJdkName;

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

        myJdkComboBox = new ComboBox<>();
        myJdkComboBox.addActionListener(e -> updateVersionLabel());
        populateJdkComboBox();
        myListenersActive = true;

        return FormBuilder.createFormBuilder()
                .addLabeledComponent(ConcordBundle.message("cli.settings.jdk.label"), myJdkComboBox)
                .addLabeledComponent(ConcordBundle.message("cli.settings.path.label"), pathPanel)
                .addLabeledComponent(ConcordBundle.message("cli.settings.version.label"), myVersionLabel)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();
    }

    private void populateJdkComboBox() {
        myJdkComboBox.removeAllItems();
        myJdkComboBox.addItem(ConcordBundle.message("cli.settings.jdk.default"));

        for (var jdk : ProjectJdkTable.getInstance().getAllJdks()) {
            if (jdk.getSdkType() instanceof JavaSdkType && jdk.getHomePath() != null) {
                myJdkComboBox.addItem(jdk.getName());
            }
        }
    }

    private @Nullable String getSelectedJdkName() {
        if (myJdkComboBox.getSelectedIndex() <= 0) {
            return null;
        }
        if (myNotFoundJdkName != null) {
            var selected = (String) myJdkComboBox.getSelectedItem();
            if (Objects.equals(selected, ConcordBundle.message("cli.settings.jdk.not.found", myNotFoundJdkName))) {
                return myNotFoundJdkName;
            }
        }
        return (String) myJdkComboBox.getSelectedItem();
    }

    private void updateVersionLabel() {
        if (!myListenersActive) {
            return;
        }

        var path = myCliPathField.getText();
        if (path.isBlank()) {
            myDetectedVersion = null;
            myVersionLabel.setText(ConcordBundle.message("cli.settings.version.not.detected"));
            return;
        }

        var generation = ++myVersionDetectionGeneration;
        myDetectedVersion = null;
        myVersionLabel.setText(ConcordBundle.message("cli.settings.version.detecting"));

        var manager = ConcordCliManager.getInstance();
        var jdkName = getSelectedJdkName();
        if (myVersionDetectionFuture != null) {
            myVersionDetectionFuture.cancel(true);
        }
        // Capture modality state on EDT before dispatching to pooled thread.
        // stateForComponent() called from a pooled thread may return NON_MODAL
        // if the component isn't yet attached to a window, causing invokeLater
        // callbacks to be deferred until the modal Settings dialog closes.
        var modality = ModalityState.current();
        myVersionDetectionFuture = ApplicationManager.getApplication().executeOnPooledThread(() -> {
            if (!manager.validateCliPath(path, jdkName)) {
                var errorKey = Files.isRegularFile(Path.of(path))
                        ? "cli.settings.path.not.executable"
                        : "cli.settings.path.not.found";
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (generation == myVersionDetectionGeneration && myVersionLabel != null) {
                        myDetectedVersion = null;
                        myVersionLabel.setText(ConcordBundle.message(errorKey));
                    }
                }, modality);
                return;
            }

            var jdkInfo = manager.resolveJdk(jdkName);
            var version = manager.detectCliVersion(path, jdkInfo);
            ApplicationManager.getApplication().invokeLater(() -> {
                if (generation == myVersionDetectionGeneration && myVersionLabel != null) {
                    myDetectedVersion = version;
                    myVersionLabel.setText(version != null ? version : ConcordBundle.message("cli.settings.version.not.detected"));
                }
            }, modality);
        });
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
            if (!fieldPath.isBlank()) {
                return true;
            }
        } else if (!currentPath.equals(fieldPath)) {
            return true;
        }

        return !Objects.equals(settings.getJdkName(), getSelectedJdkName());
    }

    @Override
    public void apply() throws ConfigurationException {
        var settings = ConcordCliSettings.getInstance();
        var path = myCliPathField.getText();
        if (!path.isBlank()) {
            if (!ConcordCliManager.getInstance().validateCliPath(path, getSelectedJdkName())) {
                throw new ConfigurationException(ConcordBundle.message("cli.settings.invalid.path"));
            }

            settings.setCliPath(path);
            settings.setCliVersion(myDetectedVersion);
        } else {
            settings.setCliPath(null);
            settings.setCliVersion(null);
        }

        settings.setJdkName(getSelectedJdkName());
    }

    @Override
    public void disposeUIResources() {
        if (myVersionDetectionFuture != null) {
            myVersionDetectionFuture.cancel(true);
            myVersionDetectionFuture = null;
        }
        myCliPathField = null;
        myVersionLabel = null;
        myJdkComboBox = null;
    }

    @Override
    public void reset() {
        myListenersActive = false;
        try {
            var settings = ConcordCliSettings.getInstance();
            var cliPath = settings.getCliPath();
            myCliPathField.setText(cliPath != null ? cliPath : "");

            populateJdkComboBox();
            myNotFoundJdkName = null;
            var jdkName = settings.getJdkName();
            if (jdkName != null) {
                myJdkComboBox.setSelectedItem(jdkName);
                if (!jdkName.equals(myJdkComboBox.getSelectedItem())) {
                    myNotFoundJdkName = jdkName;
                    var notFoundLabel = ConcordBundle.message("cli.settings.jdk.not.found", jdkName);
                    myJdkComboBox.addItem(notFoundLabel);
                    myJdkComboBox.setSelectedItem(notFoundLabel);
                }
            }
        } finally {
            myListenersActive = true;
        }
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
            var component = myVersionComboBox;
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
                    }, ModalityState.stateForComponent(component));
                } catch (IOException e) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        if (!isShowing()) {
                            return;
                        }
                        myVersionComboBox.removeAllItems();
                        myVersionComboBox.addItem(ConcordBundle.message("cli.settings.download.error", e.getMessage()));
                    }, ModalityState.stateForComponent(component));
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
