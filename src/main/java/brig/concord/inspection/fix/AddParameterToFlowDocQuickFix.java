// SPDX-License-Identifier: Apache-2.0
package brig.concord.inspection.fix;

import brig.concord.ConcordBundle;
import brig.concord.ConcordNotifications;
import brig.concord.psi.FlowDocumentation;
import com.intellij.codeInsight.intention.preview.IntentionPreviewUtils;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationAction;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.jetbrains.annotations.NotNull;

public class AddParameterToFlowDocQuickFix extends LocalQuickFixOnPsiElement {

    private final String paramName;
    private final String paramType;
    @SafeFieldForPreview
    private final SmartPsiElementPointer<FlowDocumentation> flowDocPointer;

    public AddParameterToFlowDocQuickFix(@NotNull PsiElement element, @NotNull String paramName, @NotNull String paramType, @NotNull FlowDocumentation flowDoc) {
        super(element);
        this.paramName = paramName;
        this.paramType = paramType;
        this.flowDocPointer = SmartPointerManager.getInstance(flowDoc.getProject()).createSmartPsiElementPointer(flowDoc);
    }

    @Override
    public @NotNull String getText() {
        return ConcordBundle.message("AddParameterToFlowDocQuickFix.text", paramName);
    }

    @Override
    public @NotNull String getFamilyName() {
        return ConcordBundle.message("AddParameterToFlowDocQuickFix.family");
    }

    @Override
    public void invoke(@NotNull Project project, @NotNull PsiFile file, @NotNull PsiElement startElement, @NotNull PsiElement endElement) {
        // Preview is performed in a background thread, but FlowDocumentationImpl.addInputParameter
        // modifies the document directly, which requires EDT.
        if (IntentionPreviewUtils.isIntentionPreviewActive()) {
            return;
        }

        var flowDoc = flowDocPointer.getElement();
        if (flowDoc == null) {
            return;
        }

        flowDoc.addInputParameter(paramName, paramType);

        var group = ConcordNotifications.getGroup();
        var content = ConcordBundle.message("AddParameterToFlowDocQuickFix.notification.content", paramName, flowDoc.getFlowName());
        var notification = group.createNotification(content, NotificationType.INFORMATION);

        notification.addAction(new NotificationAction(ConcordBundle.message("AddParameterToFlowDocQuickFix.notification.view")) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e, @NotNull Notification notification) {
                // Re-resolve the element to avoid memory leaks and invalid access
                var currentFlowDoc = flowDocPointer.getElement();
                if (currentFlowDoc != null) {
                    var param = currentFlowDoc.findParameter(paramName);
                    if (param != null) {
                        var descriptor = new OpenFileDescriptor(
                                project,
                                currentFlowDoc.getContainingFile().getVirtualFile(),
                                param.getTextRange().getStartOffset()
                        );
                        FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
                    }
                }
                notification.expire();
            }
        });

        notification.notify(project);
    }
}
