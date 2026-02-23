// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import brig.concord.ConcordBundle;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.TitledSeparator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Dialog for editing a single Maven repository entry.
 */
final class RepositoryEditDialog extends DialogWrapper {

    private static final String[] UPDATE_POLICIES = {"never", "always", "daily"};
    private static final String[] CHECKSUM_POLICIES = {"ignore", "warn", "fail"};
    private static final String[] PROXY_TYPES = {"http", "https"};

    private JBTextField myIdField;
    private JBTextField myUrlField;

    // Auth
    private JBTextField myUsernameField;
    private JBPasswordField myPasswordField;

    // Release Policy
    private JBCheckBox myReleaseEnabledCheckBox;
    private ComboBox<String> myReleaseUpdatePolicyCombo;
    private ComboBox<String> myReleaseChecksumPolicyCombo;

    // Snapshot Policy
    private JBCheckBox mySnapshotEnabledCheckBox;
    private ComboBox<String> mySnapshotUpdatePolicyCombo;
    private ComboBox<String> mySnapshotChecksumPolicyCombo;

    // Proxy
    private ComboBox<String> myProxyTypeCombo;
    private JBTextField myProxyHostField;
    private JBTextField myProxyPortField;

    public RepositoryEditDialog(@Nullable MvnJsonConfig.Repository repository) {
        super(true);
        setTitle(repository != null
                ? ConcordBundle.message("repositories.dialog.edit.title")
                : ConcordBundle.message("repositories.dialog.add.title"));
        init();
        if (repository != null) {
            loadFrom(repository);
        }
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        myIdField = new JBTextField();
        myUrlField = new JBTextField();

        // Auth fields
        myUsernameField = new JBTextField();
        myPasswordField = new JBPasswordField();

        // Release policy
        myReleaseEnabledCheckBox = new JBCheckBox(ConcordBundle.message("repositories.policy.enabled"), true);
        myReleaseUpdatePolicyCombo = new ComboBox<>(UPDATE_POLICIES);
        myReleaseChecksumPolicyCombo = new ComboBox<>(CHECKSUM_POLICIES);

        // Snapshot policy
        mySnapshotEnabledCheckBox = new JBCheckBox(ConcordBundle.message("repositories.policy.enabled"), true);
        mySnapshotUpdatePolicyCombo = new ComboBox<>(UPDATE_POLICIES);
        mySnapshotChecksumPolicyCombo = new ComboBox<>(CHECKSUM_POLICIES);

        // Proxy
        myProxyTypeCombo = new ComboBox<>(PROXY_TYPES);
        myProxyHostField = new JBTextField();
        myProxyPortField = new JBTextField();

        var panel = FormBuilder.createFormBuilder()
                .addLabeledComponent(ConcordBundle.message("repositories.column.id"), myIdField)
                .addLabeledComponent(ConcordBundle.message("repositories.column.url"), myUrlField)
                .addComponent(new TitledSeparator(ConcordBundle.message("repositories.dialog.auth.title")))
                .addLabeledComponent(ConcordBundle.message("repositories.auth.username"), myUsernameField)
                .addLabeledComponent(ConcordBundle.message("repositories.auth.password"), myPasswordField)
                .addComponent(new TitledSeparator(ConcordBundle.message("repositories.dialog.release.policy.title")))
                .addComponent(myReleaseEnabledCheckBox)
                .addLabeledComponent(ConcordBundle.message("repositories.policy.update"), myReleaseUpdatePolicyCombo)
                .addLabeledComponent(ConcordBundle.message("repositories.policy.checksum"), myReleaseChecksumPolicyCombo)
                .addComponent(new TitledSeparator(ConcordBundle.message("repositories.dialog.snapshot.policy.title")))
                .addComponent(mySnapshotEnabledCheckBox)
                .addLabeledComponent(ConcordBundle.message("repositories.policy.update"), mySnapshotUpdatePolicyCombo)
                .addLabeledComponent(ConcordBundle.message("repositories.policy.checksum"), mySnapshotChecksumPolicyCombo)
                .addComponent(new TitledSeparator(ConcordBundle.message("repositories.dialog.proxy.title")))
                .addLabeledComponent(ConcordBundle.message("repositories.proxy.type"), myProxyTypeCombo)
                .addLabeledComponent(ConcordBundle.message("repositories.proxy.host"), myProxyHostField)
                .addLabeledComponent(ConcordBundle.message("repositories.proxy.port"), myProxyPortField)
                .getPanel();

        panel.setPreferredSize(JBUI.size(450, -1));
        return panel;
    }

    @Override
    protected @Nullable ValidationInfo doValidate() {
        if (myIdField.getText().isBlank()) {
            return new ValidationInfo(
                    ConcordBundle.message("repositories.validation.id.empty"), myIdField);
        }
        if (myUrlField.getText().isBlank()) {
            return new ValidationInfo(
                    ConcordBundle.message("repositories.validation.url.empty"), myUrlField);
        }
        var portText = myProxyPortField.getText().trim();
        if (!portText.isEmpty()) {
            try {
                var port = Integer.parseInt(portText);
                if (port < 0 || port > 65535) {
                    return new ValidationInfo(
                            ConcordBundle.message("repositories.validation.port.invalid"), myProxyPortField);
                }
            } catch (NumberFormatException e) {
                return new ValidationInfo(
                        ConcordBundle.message("repositories.validation.port.invalid"), myProxyPortField);
            }
        }
        return null;
    }

    private void loadFrom(@NotNull MvnJsonConfig.Repository repo) {
        if (repo.getId() != null) {
            myIdField.setText(repo.getId());
        }
        if (repo.getUrl() != null) {
            myUrlField.setText(repo.getUrl());
        }

        var auth = repo.getAuth();
        if (auth != null) {
            if (auth.getUsername() != null) {
                myUsernameField.setText(auth.getUsername());
            }
            if (auth.getPassword() != null) {
                myPasswordField.setText(auth.getPassword());
            }
        }

        var release = repo.getReleasePolicy();
        if (release != null) {
            myReleaseEnabledCheckBox.setSelected(release.isEnabled());
            myReleaseUpdatePolicyCombo.setSelectedItem(release.getUpdatePolicy());
            myReleaseChecksumPolicyCombo.setSelectedItem(release.getChecksumPolicy());
        }

        var snapshot = repo.getSnapshotPolicy();
        if (snapshot != null) {
            mySnapshotEnabledCheckBox.setSelected(snapshot.isEnabled());
            mySnapshotUpdatePolicyCombo.setSelectedItem(snapshot.getUpdatePolicy());
            mySnapshotChecksumPolicyCombo.setSelectedItem(snapshot.getChecksumPolicy());
        }

        var proxy = repo.getProxy();
        if (proxy != null) {
            myProxyTypeCombo.setSelectedItem(proxy.getType());
            if (proxy.getHost() != null) {
                myProxyHostField.setText(proxy.getHost());
            }
            if (proxy.getPort() != null) {
                myProxyPortField.setText(proxy.getPort().toString());
            }
        }
    }

    /**
     * Builds a Repository from the current dialog state.
     */
    public @NotNull MvnJsonConfig.Repository getRepository() {
        var repo = new MvnJsonConfig.Repository();
        repo.setId(myIdField.getText().trim());
        repo.setUrl(myUrlField.getText().trim());

        // Auth
        var username = myUsernameField.getText().trim();
        var password = new String(myPasswordField.getPassword()).trim();
        if (!username.isEmpty() || !password.isEmpty()) {
            repo.setAuth(new MvnJsonConfig.AuthConfig(
                    username.isEmpty() ? null : username,
                    password.isEmpty() ? null : password
            ));
        }

        // Release policy
        repo.setReleasePolicy(buildPolicy(
                myReleaseEnabledCheckBox, myReleaseUpdatePolicyCombo, myReleaseChecksumPolicyCombo));

        // Snapshot policy
        repo.setSnapshotPolicy(buildPolicy(
                mySnapshotEnabledCheckBox, mySnapshotUpdatePolicyCombo, mySnapshotChecksumPolicyCombo));

        // Proxy
        var proxyHost = myProxyHostField.getText().trim();
        if (!proxyHost.isEmpty()) {
            var proxy = new MvnJsonConfig.ProxyConfig();
            proxy.setType((String) myProxyTypeCombo.getSelectedItem());
            proxy.setHost(proxyHost);
            var portText = myProxyPortField.getText().trim();
            if (!portText.isEmpty()) {
                proxy.setPort(Integer.parseInt(portText));
            }
            repo.setProxy(proxy);
        }

        return repo;
    }

    private static @Nullable MvnJsonConfig.PolicyConfig buildPolicy(
            @NotNull JBCheckBox enabledCheckBox,
            @NotNull ComboBox<String> updatePolicyCombo,
            @NotNull ComboBox<String> checksumPolicyCombo) {
        var enabled = enabledCheckBox.isSelected();
        var updatePolicy = (String) updatePolicyCombo.getSelectedItem();
        var checksumPolicy = (String) checksumPolicyCombo.getSelectedItem();

        // null means "not configured, use defaults" — avoid false positives in isModified()
        if (enabled && "never".equals(updatePolicy) && "ignore".equals(checksumPolicy)) {
            return null;
        }

        return new MvnJsonConfig.PolicyConfig(enabled, updatePolicy, checksumPolicy);
    }
}
