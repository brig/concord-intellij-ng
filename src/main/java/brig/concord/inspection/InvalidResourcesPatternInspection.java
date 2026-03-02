// SPDX-License-Identifier: Apache-2.0
package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordResourcePatterns;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLSequence;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

/**
 * Validates path patterns in top-level resources.concord list.
 */
public class InvalidResourcesPatternInspection extends ConcordInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildConcordVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull ConcordFile concordFile) {
        var virtualFile = concordFile.getVirtualFile();
        if (virtualFile == null) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        var resourcesKv = concordFile.resources().orElse(null);
        if (resourcesKv == null) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        var resourcesValue = resourcesKv.getValue();
        if (!(resourcesValue instanceof YAMLMapping resourcesMapping)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        var concordKv = resourcesMapping.getKeyValueByKey("concord");
        if (concordKv == null) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        var concordValue = concordKv.getValue();
        if (!(concordValue instanceof YAMLSequence sequence)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        var rootDirPrefix = ConcordResourcePatterns.rootDirPrefix(virtualFile);

        return new PsiElementVisitor() {
            @Override
            public void visitFile(@NotNull PsiFile file) {
                for (var item : sequence.getItems()) {
                    if (!(item.getValue() instanceof YAMLScalar scalar)) {
                        continue;
                    }

                    var pattern = scalar.getTextValue().trim();
                    if (pattern.isBlank()) {
                        continue;
                    }

                    if (!isValidPattern(pattern, rootDirPrefix)) {
                        holder.registerProblem(
                                scalar,
                                ConcordBundle.message("inspection.invalid.resources.pattern.message", pattern),
                                ProblemHighlightType.GENERIC_ERROR_OR_WARNING
                        );
                    }
                }
            }
        };
    }

    private static boolean isValidPattern(@NotNull String pattern, @NotNull String rootDirPrefix) {
        try {
            ConcordResourcePatterns.parsePattern(pattern, rootDirPrefix);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
