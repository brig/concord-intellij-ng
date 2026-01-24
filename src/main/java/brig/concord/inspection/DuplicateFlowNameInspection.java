package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.inspection.fix.ShowAllDuplicateFlowsFix;
import brig.concord.psi.*;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLSequence;
import brig.concord.yaml.psi.YamlPsiElementVisitor;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Inspection that detects when multiple flow definitions with the same name
 * exist in the same Concord scope.
 */
public class DuplicateFlowNameInspection extends LocalInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                                   boolean isOnTheFly) {
        if (!(holder.getFile() instanceof ConcordFile)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        return new YamlPsiElementVisitor() {
            @Override
            public void visitKeyValue(@NotNull YAMLKeyValue keyValue) {
                checkForDuplicateFlowName(keyValue, holder);
            }

            @Override
            public void visitSequence(@NotNull YAMLSequence sequence) {
                // Do not visit children
            }
        };
    }

    private static void checkForDuplicateFlowName(@NotNull YAMLKeyValue keyValue,
                                                  @NotNull ProblemsHolder holder) {
        if (!ProcessDefinition.isFlowDefinition(keyValue)) {
            return;
        }

        var keyElement = keyValue.getKey();
        if (keyElement == null) {
            return;
        }

        var flowName = keyValue.getKeyText();
        if (flowName.isBlank()) {
            return;
        }

        var process = ProcessDefinitionProvider.getInstance().get(keyValue);
        if (process == null) {
            return;
        }

        var currentFile = keyValue.getContainingFile().getVirtualFile();
        for (var flow : process.flows(flowName)) {
            var flowFile = flow.getContainingFile().getVirtualFile();
            if (!flowFile.equals(currentFile)) {
                var relativePath = getRelativePath(keyValue.getProject(), flowFile);
                holder.registerProblem(
                        keyElement,
                        ConcordBundle.message("inspection.duplicate.flow.name.message", flowName, relativePath),
                        ProblemHighlightType.WARNING,
                        new ShowAllDuplicateFlowsFix()
                );
                // Only report first duplicate to avoid noise
                break;
            }
        }
    }

    private static String getRelativePath(Project project, VirtualFile file) {
        var projectDir = project.getBasePath();
        if (projectDir != null) {
            var rootPath = Path.of(projectDir);
            var filePath = Path.of(file.getPath());
            if (filePath.startsWith(rootPath)) {
                return rootPath.relativize(filePath).toString();
            }
        }
        return file.getName();
    }
}
