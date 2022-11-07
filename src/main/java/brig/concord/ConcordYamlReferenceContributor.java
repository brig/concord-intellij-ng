package brig.concord;

import brig.concord.psi.impl.yaml.YAMLConcordKeyValueImpl;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class ConcordYamlReferenceContributor extends PsiReferenceContributor {
    private PatternCondition<PsiElement> keyValues = new PatternCondition<>("YAMLConcordKeyValueImpl") {
        @Override
        public boolean accepts(@NotNull PsiElement element,
                               ProcessingContext context) {
            return element instanceof YAMLConcordKeyValueImpl;
        }
    };

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(PlatformPatterns.psiElement().with(keyValues),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        return element.getReferences();
                    }
                });
    }
}
