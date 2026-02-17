package brig.concord;

import brig.concord.el.psi.ElIdentifierExpr;
import brig.concord.el.psi.ElMemberName;
import brig.concord.psi.ElAccessChainExtractor;
import brig.concord.psi.ElPropertyProvider;
import brig.concord.psi.VariablesProvider;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(ElIdentifierExpr.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        if (!(element instanceof ElIdentifierExpr identExpr)) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        return new PsiReference[]{new ElVarRef(identExpr)};
                    }
                });

        registrar.registerReferenceProvider(
                PlatformPatterns.psiElement(ElMemberName.class),
                new PsiReferenceProvider() {
                    @Override
                    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                                           @NotNull ProcessingContext context) {
                        if (!(element instanceof ElMemberName memberName)) {
                            return PsiReference.EMPTY_ARRAY;
                        }
                        return new PsiReference[]{new ElMemberRef(memberName)};
                    }
                });
    }

    private static class ElVarRef extends PsiReferenceBase<ElIdentifierExpr> {

        ElVarRef(@NotNull ElIdentifierExpr element) {
            super(element, TextRange.from(0, element.getTextLength()), true);
        }

        @Override
        public @Nullable PsiElement resolve() {
            var yamlElement = PsiTreeUtil.getParentOfType(getElement(), YAMLValue.class);
            if (yamlElement == null) {
                return null;
            }

            var name = getElement().getIdentifier().getText();
            var variables = VariablesProvider.getVariables(yamlElement);
            for (var variable : variables) {
                if (name.equals(variable.name())) {
                    return variable.declaration();
                }
            }
            return null;
        }
    }

    private static class ElMemberRef extends PsiReferenceBase<ElMemberName> {

        ElMemberRef(@NotNull ElMemberName element) {
            super(element, TextRange.from(0, element.getTextLength()), true);
        }

        @Override
        public @Nullable PsiElement resolve() {
            var yamlElement = PsiTreeUtil.getParentOfType(getElement(), YAMLValue.class);
            if (yamlElement == null) {
                return null;
            }

            var segments = ElAccessChainExtractor.extractChainSegments(getElement());
            if (segments.isEmpty()) {
                return null;
            }

            var propertyName = getElement().getText();
            var provider = ElPropertyProvider.getInstance(yamlElement.getProject());
            return provider.resolveProperty(segments, propertyName, yamlElement);
        }
    }
}
