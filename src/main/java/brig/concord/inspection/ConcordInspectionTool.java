package brig.concord.inspection;

import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordScopeService;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public abstract class ConcordInspectionTool extends LocalInspectionTool {

    @Override
    public final @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        if (!(holder.getFile() instanceof ConcordFile)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        if (ConcordScopeService.getInstance(holder.getProject()).isIgnored(holder.getFile())) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        return buildConcordVisitor(holder, isOnTheFly);
    }

    @Override
    public final @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return buildVisitor(holder, isOnTheFly);
    }

    public abstract @NotNull PsiElementVisitor buildConcordVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly);
}
