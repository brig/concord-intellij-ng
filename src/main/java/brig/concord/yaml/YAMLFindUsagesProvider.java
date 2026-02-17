// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml;

import brig.concord.ConcordBundle;
import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.*;

/**
 * @author shalupov
 */
public class YAMLFindUsagesProvider implements FindUsagesProvider {
    @Override
    public @NotNull WordsScanner getWordsScanner() {
        return new YAMLWordsScanner();
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof PsiNamedElement || psiElement instanceof YAMLScalar;
    }

    @Override
    public @NotNull String getHelpId(@NotNull PsiElement psiElement) {
        return HelpID.FIND_OTHER_USAGES;
    }

    @Override
    public @NotNull String getType(@NotNull PsiElement element) {
        if (element instanceof YAMLScalarText) {
            return ConcordBundle.message("find.usages.scalar");
        } else if (element instanceof YAMLSequence) {
            return ConcordBundle.message("find.usages.sequence");
        } else if (element instanceof YAMLMapping) {
            return ConcordBundle.message("find.usages.mapping");
        } else if (element instanceof YAMLKeyValue) {
            return ConcordBundle.message("find.usages.key.value");
        } else if (element instanceof YAMLDocument) {
            return ConcordBundle.message("find.usages.document");
        } else {
            return "";
        }
    }

    @Override
    public @NotNull String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof PsiNamedElement) {
            return StringUtil.notNullize(((PsiNamedElement)element).getName(), ConcordBundle.message("find.usages.unnamed"));
        }

        if (element instanceof YAMLScalar) {
            return ((YAMLScalar)element).getTextValue();
        }

        return ConcordBundle.message("find.usages.unnamed");
    }

    @Override
    public @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        return getDescriptiveName(element);
    }
}

