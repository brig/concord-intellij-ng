// SPDX-License-Identifier: Apache-2.0
package brig.concord;

import brig.concord.el.psi.*;
import brig.concord.psi.ElAccessChainResolver;
import brig.concord.psi.VariablesProvider;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
                        return new PsiReference[]{new ElPropertyRef(memberName)};
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

    private static class ElPropertyRef extends PsiReferenceBase<ElMemberName> {

        ElPropertyRef(@NotNull ElMemberName element) {
            super(element, TextRange.from(0, element.getTextLength()), true);
        }

        @Override
        public @Nullable PsiElement resolve() {
            var memberName = getElement();
            var variable = ElAccessChainResolver.resolveVariable(memberName);
            if (variable == null || variable.declaration() == null) {
                return null;
            }

            var chain = buildPropertyChain(memberName);
            if (chain.isEmpty()) {
                return null;
            }

            // Walk the YAML mapping tree following the property names
            PsiElement current = variable.declaration();

            // If the declaration is a YAMLKeyValue, start from its value
            if (current instanceof YAMLKeyValue kv) {
                current = kv.getValue();
            }

            for (int i = 0; i < chain.size(); i++) {
                if (!(current instanceof YAMLMapping mapping)) {
                    return null;
                }

                var kv = mapping.getKeyValueByKey(chain.get(i));
                if (kv == null) {
                    return null;
                }

                if (i == chain.size() - 1) {
                    return kv;
                }

                current = kv.getValue();
            }

            return null;
        }

        /**
         * Builds the list of property names from the base variable to the target member name.
         * For {@code config.db.host}, returns ["db", "host"] if memberName is "host".
         */
        private static @NotNull List<String> buildPropertyChain(@NotNull ElMemberName targetMember) {
            var dotSuffix = targetMember.getParent();
            if (!(dotSuffix instanceof ElDotSuffix)) {
                return List.of();
            }

            var accessExpr = dotSuffix.getParent();
            if (!(accessExpr instanceof ElAccessExpr access)) {
                return List.of();
            }

            var chain = new ArrayList<String>();
            for (var suffix : access.getSuffixList()) {
                if (!(suffix instanceof ElDotSuffix ds)) {
                    return List.of();
                }

                var mn = ds.getMemberName();
                if (mn == null || mn.getIdentifier() == null) {
                    return List.of();
                }

                chain.add(mn.getIdentifier().getText());

                if (suffix == dotSuffix) {
                    break;
                }
            }

            return chain;
        }
    }
}
