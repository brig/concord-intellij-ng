// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.inspection.fix;

import brig.concord.ConcordBundle;
import brig.concord.psi.FlowDocParameter;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class ReplaceFlowDocKeywordQuickFix implements LocalQuickFix {

    private final String myNewKeyword;

    public ReplaceFlowDocKeywordQuickFix(String newKeyword) {
        myNewKeyword = newKeyword;
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getName() {
        return ConcordBundle.message("inspection.flow.doc.fix.keyword", myNewKeyword);
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
        return ConcordBundle.message("inspection.flow.doc.fix.keyword.family");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        var element = descriptor.getPsiElement();
        var param = PsiTreeUtil.getParentOfType(element, FlowDocParameter.class);
        if (param != null) {
            param.setKeyword(myNewKeyword);
        }
    }
}
