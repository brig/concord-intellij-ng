// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.inspection.fix;

import brig.concord.ConcordBundle;
import brig.concord.yaml.YAMLUtil;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.util.IntentionFamilyName;
import com.intellij.codeInspection.util.IntentionName;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class InsertClosingMarkerFix implements IntentionAction {

    @SafeFieldForPreview
    private final SmartPsiElementPointer<PsiElement> errorElementPointer;

    public InsertClosingMarkerFix(@NotNull PsiElement errorElement) {
        this.errorElementPointer = SmartPointerManager.createPointer(errorElement);
    }

    @Override
    public @IntentionName @NotNull String getText() {
        return ConcordBundle.message("inspection.flow.doc.fix.insert.closing.marker");
    }

    @Override
    public @IntentionFamilyName @NotNull String getFamilyName() {
        return ConcordBundle.message("inspection.flow.doc.fix.insert.closing.marker");
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        var element = errorElementPointer.getElement();
        return element != null && element.isValid();
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        var errorElement = errorElementPointer.getElement();
        if (errorElement == null || !errorElement.isValid()) {
            return;
        }

        var document = file.getViewProvider().getDocument();
        if (document == null) {
            return;
        }

        var errorOffset = errorElement.getTextRange().getStartOffset();
        var lineNumber = document.getLineNumber(errorOffset);
        var lineStartOffset = document.getLineStartOffset(lineNumber);

        var indentLength = YAMLUtil.getIndentToThisElement(errorElement);
        var indent = " ".repeat(indentLength);

        var closingMarker = indent + "##\n";
        document.insertString(lineStartOffset, closingMarker);
    }

    @Override
    public boolean startInWriteAction() {
        return true;
    }
}
