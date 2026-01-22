package brig.concord.inspection.fix;

import brig.concord.ConcordBundle;
import brig.concord.psi.FlowDocParameter;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ReplaceFlowDocTypeQuickFix implements LocalQuickFix {

    private final String myNewType;

    public ReplaceFlowDocTypeQuickFix(String newType) {
        myNewType = newType;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getName() {
        return ConcordBundle.message("inspection.flow.doc.fix.type", myNewType);
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
        return ConcordBundle.message("inspection.flow.doc.fix.type.family");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        var element = descriptor.getPsiElement();
        if (element instanceof FlowDocParameter param) {
            param.setType(myNewType);
        }
    }
}
