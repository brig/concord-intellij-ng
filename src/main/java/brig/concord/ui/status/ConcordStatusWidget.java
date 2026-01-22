package brig.concord.ui.status;

import brig.concord.psi.ConcordRoot;
import brig.concord.psi.ConcordScopeService;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.StatusBarWidget;
import com.intellij.util.Consumer;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;

public class ConcordStatusWidget implements StatusBarWidget, StatusBarWidget.TextPresentation {

    private final Project project;
    private StatusBar statusBar;
    private String currentScope;

    public ConcordStatusWidget(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String ID() {
        return "ConcordStatusWidget";
    }

    @Override
    public void install(@NotNull StatusBar statusBar) {
        this.statusBar = statusBar;
        MessageBusConnection connection = project.getMessageBus().connect(this);
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                updateScope(event.getNewFile());
            }
        });

        // Initialize with current file
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        VirtualFile[] selectedFiles = fileEditorManager.getSelectedFiles();
        if (selectedFiles.length > 0) {
            updateScope(selectedFiles[0]);
        }
    }

    private void updateScope(@Nullable VirtualFile file) {
        if (file == null) {
            currentScope = null;
        } else {
            ConcordRoot scope = ConcordScopeService.getInstance(project).getPrimaryScope(file);
            currentScope = scope != null ? scope.getScopeName() : null;
        }
        if (statusBar != null) {
            statusBar.updateWidget(ID());
        }
    }

    @Override
    public @NotNull String getText() {
        if (currentScope != null) {
            return "Concord: " + currentScope;
        }
        return "Concord";
    }

    @Override
    public String getTooltipText() {
        return "Concord runtime status";
    }

    @Override
    public @Nullable Consumer<MouseEvent> getClickConsumer() {
        return e -> {
            // open popup / settings
        };
    }

    @Override
    public void dispose() {}

    @Override
    public float getAlignment() {
        return 0;
    }

    @Override
    public @NotNull WidgetPresentation getPresentation() {
        return this;
    }
}

