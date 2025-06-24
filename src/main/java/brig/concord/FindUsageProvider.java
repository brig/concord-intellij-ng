package brig.concord;

import brig.concord.psi.ConcordFile;
import brig.concord.psi.impl.yaml.YAMLConcordKeyValueImpl;
import brig.concord.psi.impl.yaml.YAMLConcordPlainTextImpl;
import brig.concord.psi.impl.yaml.YAMLConcordQuotedTextImpl;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.YAMLFindUsagesProvider;
import brig.concord.yaml.YAMLWordsScanner;

public class FindUsageProvider extends YAMLFindUsagesProvider implements FindUsagesProvider {

    @Override
    public @Nullable WordsScanner getWordsScanner() {
        return new YAMLWordsScanner() {
            @Override
            public int getVersion() {
                return 10000;
            }
        };
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        if (!(psiElement.getContainingFile() instanceof ConcordFile)) {
            return false;
        }

        return psiElement instanceof YAMLConcordPlainTextImpl
                || psiElement instanceof YAMLConcordQuotedTextImpl
                || psiElement instanceof YAMLConcordKeyValueImpl;
    }

    @Override
    public @Nullable @NonNls String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public @Nls @NotNull String getType(@NotNull PsiElement element) {
        return "Flow";
    }

    @Override
    public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement element) {
        final String name = element instanceof PsiNamedElement ? ((PsiNamedElement)element).getName() : null;
        return name != null ? name : "unnamed";
    }

    @Override
    public @Nls @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {

        if (element instanceof YAMLConcordKeyValueImpl || element instanceof YAMLConcordPlainTextImpl) {
            return getDescriptiveName(element);
        }

        return super.getNodeText(element, useFullName);
    }
}
