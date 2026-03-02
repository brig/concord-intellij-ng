// SPDX-License-Identifier: Apache-2.0
package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordResourcePatterns;
import brig.concord.yaml.psi.YAMLKeyValue;
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

        var sequence = concordFile.resources()
                .map(YAMLKeyValue::getValue)
                .filter(YAMLMapping.class::isInstance)
                .map(YAMLMapping.class::cast)
                .map(m -> m.getKeyValueByKey("concord"))
                .map(YAMLKeyValue::getValue)
                .filter(YAMLSequence.class::isInstance)
                .map(YAMLSequence.class::cast)
                .orElse(null);

        if (sequence == null) {
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
        } catch (Exception e) {
            return false;
        }
    }
}
