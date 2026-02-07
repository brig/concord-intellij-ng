package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.dependency.DependencyCollector;
import brig.concord.dependency.MavenCoordinate;
import brig.concord.dependency.TaskRegistry;
import brig.concord.psi.ConcordFile;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class UnresolvedDependencyInspection extends ConcordInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildConcordVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                if (!(file instanceof ConcordFile concordFile)) {
                    return;
                }

                var failedDeps = TaskRegistry.getInstance(holder.getProject()).getFailedDependencies();

                DependencyCollector.forEachDependencyScalar(concordFile, scalar -> {
                    var coordinate = MavenCoordinate.parse(scalar.getTextValue());
                    if (coordinate == null) {
                        holder.registerProblem(
                                scalar,
                                ConcordBundle.message("inspection.invalid.dependency.format.message",
                                        scalar.getTextValue()),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        );
                        return;
                    }

                    var errorMessage = failedDeps.get(coordinate);
                    if (errorMessage != null) {
                        holder.registerProblem(
                                scalar,
                                ConcordBundle.message("inspection.unresolved.dependency.message",
                                        scalar.getTextValue(), errorMessage),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        );
                    }
                });
            }
        };
    }
}
