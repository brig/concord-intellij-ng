package brig.concord.inspection.fix;

import brig.concord.ConcordBundle;
import brig.concord.psi.FlowDocumentation;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
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
        var flowDoc = flowDocPointer.getElement();
        if (flowDoc == null) {
            return;
        }

        flowDoc.addInputParameter(paramName, paramType);
    }
}
