// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import brig.concord.ConcordBundle;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.JBTable;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Settings page for Concord repository configuration (Tools > Concord > Repositories).
 */
public final class ConcordRepositoriesConfigurable implements Configurable {

    private static final Logger LOG = Logger.getInstance(ConcordRepositoriesConfigurable.class);

    private TextFieldWithBrowseButton myDepsCachePathField;
    private JBTable myReposTable;
    private RepositoryTableModel myTableModel;
    private List<MvnJsonConfig.Repository> mySavedRepositories = List.of();

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
        return ConcordBundle.message("repositories.settings.display.name");
    }

    @Override
    public @Nullable JComponent createComponent() {
        myDepsCachePathField = new TextFieldWithBrowseButton();
        var dirDescriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
                .withTitle(ConcordBundle.message("repositories.browse.deps.cache.title"));
        myDepsCachePathField.addBrowseFolderListener(null, dirDescriptor);

        myTableModel = new RepositoryTableModel();
        myReposTable = new JBTable(myTableModel);
        myReposTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        var decorator = ToolbarDecorator.createDecorator(myReposTable)
                .setAddAction(b -> addRepository())
                .setRemoveAction(b -> removeRepository())
                .setEditAction(b -> editRepository())
                .setEditActionUpdater(e -> myReposTable.getSelectedRow() >= 0)
                .disableUpDownActions();

        var mvnJsonPath = ConcordRepositorySettings.getInstance().getEffectiveMvnJsonPath().toString();

        return FormBuilder.createFormBuilder()
                .addLabeledComponent(ConcordBundle.message("repositories.deps.cache.path.label"), myDepsCachePathField)
                .addLabeledComponent(ConcordBundle.message("repositories.mvn.json.path.label"), new JLabel(mvnJsonPath), UIUtil.LARGE_VGAP)
                .addVerticalGap(UIUtil.LARGE_VGAP)
                .addLabeledComponentFillVertically(ConcordBundle.message("repositories.table.label"), decorator.createPanel())
                .getPanel();
    }

    private void addRepository() {
        var dialog = new RepositoryEditDialog(null);
        if (dialog.showAndGet()) {
            myTableModel.addRepository(dialog.getRepository());
        }
    }

    private void removeRepository() {
        var row = myReposTable.getSelectedRow();
        if (row >= 0) {
            myTableModel.removeRepository(row);
        }
    }

    private void editRepository() {
        var row = myReposTable.getSelectedRow();
        if (row < 0) {
            return;
        }
        var existing = myTableModel.getRepository(row);
        var dialog = new RepositoryEditDialog(existing);
        if (dialog.showAndGet()) {
            myTableModel.setRepository(row, dialog.getRepository());
        }
    }

    @Override
    public boolean isModified() {
        var settings = ConcordRepositorySettings.getInstance();

        var savedPath = settings.getDepsCachePath() != null
                ? settings.getDepsCachePath()
                : ConcordRepositorySettings.getDefaultDepsCachePath();
        if (!Objects.equals(nullIfBlank(myDepsCachePathField.getText()), savedPath)) {
            return true;
        }

        if (!myTableModel.getRepositories().equals(mySavedRepositories)) {
            return true;
        }

        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        if (nullIfBlank(myDepsCachePathField.getText()) == null) {
            throw new ConfigurationException(
                    ConcordBundle.message("repositories.validation.deps.cache.empty"));
        }

        var settings = ConcordRepositorySettings.getInstance();
        settings.setDepsCachePath(nullIfBlank(myDepsCachePathField.getText()));

        var mvnJsonPath = settings.getEffectiveMvnJsonPath();
        var repos = myTableModel.getRepositories();
        var config = new MvnJsonConfig(new ArrayList<>(repos));
        try {
            MvnJsonParser.write(mvnJsonPath, config);
        } catch (IOException e) {
            throw new ConfigurationException(
                    ConcordBundle.message("repositories.error.write", e.getMessage()));
        }
        mySavedRepositories = List.copyOf(repos);
    }

    @Override
    public void reset() {
        var settings = ConcordRepositorySettings.getInstance();
        myDepsCachePathField.setText(
                settings.getDepsCachePath() != null ? settings.getDepsCachePath()
                        : ConcordRepositorySettings.getDefaultDepsCachePath());

        var mvnJsonPath = settings.getEffectiveMvnJsonPath();
        try {
            var config = MvnJsonParser.read(mvnJsonPath);
            mySavedRepositories = List.copyOf(config.getRepositories());
            myTableModel.setRepositories(config.getRepositories());
        } catch (Exception e) {
            LOG.warn("Failed to read mvn.json from " + mvnJsonPath, e);
            mySavedRepositories = List.of();
            myTableModel.setRepositories(List.of());
        }
    }

    @Override
    public void disposeUIResources() {
        myDepsCachePathField = null;
        myReposTable = null;
        myTableModel = null;
        mySavedRepositories = List.of();
    }

    private static @Nullable String nullIfBlank(@Nullable String s) {
        return s != null && !s.isBlank() ? s : null;
    }

    private static final class RepositoryTableModel extends AbstractTableModel {

        private final List<MvnJsonConfig.Repository> myRepositories = new ArrayList<>();

        @Override
        public int getRowCount() {
            return myRepositories.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int column) {
            return switch (column) {
                case 0 -> ConcordBundle.message("repositories.column.id");
                case 1 -> ConcordBundle.message("repositories.column.url");
                default -> "";
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            var repo = myRepositories.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> repo.getId();
                case 1 -> repo.getUrl();
                default -> "";
            };
        }

        public void addRepository(MvnJsonConfig.Repository repo) {
            myRepositories.add(repo);
            fireTableRowsInserted(myRepositories.size() - 1, myRepositories.size() - 1);
        }

        public void removeRepository(int row) {
            myRepositories.remove(row);
            fireTableRowsDeleted(row, row);
        }

        public MvnJsonConfig.Repository getRepository(int row) {
            return myRepositories.get(row);
        }

        public void setRepository(int row, MvnJsonConfig.Repository repo) {
            myRepositories.set(row, repo);
            fireTableRowsUpdated(row, row);
        }

        public List<MvnJsonConfig.Repository> getRepositories() {
            return List.copyOf(myRepositories);
        }

        public void setRepositories(List<MvnJsonConfig.Repository> repos) {
            myRepositories.clear();
            for (var repo : repos) {
                myRepositories.add(repo.copy());
            }
            fireTableDataChanged();
        }
    }
}
