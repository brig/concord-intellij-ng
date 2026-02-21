// SPDX-License-Identifier: Apache-2.0
package brig.concord.notification;

import brig.concord.ConcordBundle;
import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordScopeService;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Function;

/**
 * Shows a warning banner at the top of the editor when a Concord file
 * is not in any Concord scope.
 */
public class OutOfScopeEditorNotificationProvider implements EditorNotificationProvider, DumbAware {

    private static final Key<Boolean> DISMISSED_KEY = Key.create("concord.out.of.scope.dismissed");

    @Override
    public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(
            @NotNull Project project,
            @NotNull VirtualFile file) {

        // Check if notification was dismissed for this file
        if (Boolean.TRUE.equals(file.getUserData(DISMISSED_KEY))) {
            return null;
        }

        // Check if it's a Concord file
        var psiFile = PsiManager.getInstance(project).findFile(file);
        if (!(psiFile instanceof ConcordFile)) {
            return null;
        }

        // Check if file is in any scope
        if (!isOutOfScope(file, project)) {
            return null;
        }

        // File is out of scope, show notification
        return fileEditor -> createPanel(project, file);
    }

    private static boolean isOutOfScope(@NotNull VirtualFile file, @NotNull Project project) {
        var service = ConcordScopeService.getInstance(project);
        if (service.isIgnored(file)) {
            return true;
        }
        return service.isOutOfScope(file);
    }

    private @NotNull EditorNotificationPanel createPanel(@NotNull Project project, @NotNull VirtualFile file) {
        var panel = new EditorNotificationPanel(EditorNotificationPanel.Status.Warning);
        panel.setText(ConcordBundle.message("notification.out.of.scope.message"));

        panel.createActionLabel(ConcordBundle.message("notification.out.of.scope.dismiss"), () -> {
            file.putUserData(DISMISSED_KEY, Boolean.TRUE);
            EditorNotifications.getInstance(project).updateNotifications(file);
        });

        return panel;
    }
}
