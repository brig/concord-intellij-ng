package brig.concord;

import brig.concord.psi.impl.yaml.YAMLConcordKeyValueImpl;
import brig.concord.psi.impl.yaml.YAMLConcordPlainTextImpl;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFindUsagesProvider;

public class FindUsageProvider extends YAMLFindUsagesProvider implements FindUsagesProvider {

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return true;
    }

    @Override
    public @Nullable @NonNls String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public @Nls @NotNull String getType(@NotNull PsiElement element) {
        System.out.println("<<<< gettype");
        return super.getType(element);
    }

    @Override
    public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement element) {
        System.out.println("<<<< getDescriptiveName");
        return super.getDescriptiveName(element);
    }

    @Override
    public @Nls @NotNull String getNodeText(@NotNull PsiElement element,
                                            boolean useFullName) {
        System.out.println("<<<< getNodeText");

        if (element instanceof YAMLConcordKeyValueImpl || element instanceof YAMLConcordPlainTextImpl) {
            return getDescriptiveName(element);
        }

        return super.getNodeText(element, useFullName);
    }
}
