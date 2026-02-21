// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.inspection;

import brig.concord.ConcordBundle;
import brig.concord.psi.ConcordScopeService;
import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YamlPsiElementVisitor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

/**
 * Inspection that warns when a Concord file is not in any Concord scope.
 * A file is "out of scope" when it's not a root file itself and
 * ConcordScopeService.getScopesForFile() returns an empty list.
 */
public class OutOfScopeInspection extends ConcordInspectionTool {

    @Override
    public @NotNull PsiElementVisitor buildConcordVisitor(@NotNull ProblemsHolder holder,
                                                           boolean isOnTheFly) {

        var virtualFile = holder.getFile().getVirtualFile();
        if (virtualFile == null) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        var service = ConcordScopeService.getInstance(holder.getProject());
        if (!service.isOutOfScope(virtualFile)) {
            return PsiElementVisitor.EMPTY_VISITOR;
        }

        // File is out of scope, register a warning on the document
        return new YamlPsiElementVisitor() {
            @Override
            public void visitDocument(@NotNull YAMLDocument document) {
                var topLevelValue = document.getTopLevelValue();
                // Skip empty documents - can't register problems on empty PSI elements
                if (topLevelValue == null || topLevelValue.getTextLength() == 0) {
                    return;
                }

                holder.registerProblem(
                        topLevelValue,
                        ConcordBundle.message("inspection.out.of.scope.message")
                );
            }
        };
    }
}
