// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.completion;

import brig.concord.el.psi.ElFunctionExpr;
import brig.concord.el.psi.ElIdentifierExpr;
import brig.concord.el.psi.ElMemberName;
import brig.concord.el.psi.ElTypes;
import brig.concord.psi.ElAccessChainResolver;
import brig.concord.psi.VariableSource;
import brig.concord.psi.VariablesProvider;
import brig.concord.schema.SchemaProperty;
import brig.concord.schema.SchemaType;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class ElCompletionContributor extends CompletionContributor {

    public record VariableLookup(@NotNull String name, @NotNull VariableSource source,
                                 @NotNull SchemaProperty schema) {
    }

    public record PropertyLookup(@NotNull String name, @NotNull SchemaProperty schema) {
    }

    public ElCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(ElTypes.IDENTIFIER),
                new ElVariableCompletionProvider());

        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(ElTypes.IDENTIFIER),
                new ElPropertyCompletionProvider());
    }

    private static class ElVariableCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            var position = parameters.getPosition();

            // Skip completion after dot (property access) or in function names
            var parent = position.getParent();
            if (parent instanceof ElMemberName || parent instanceof ElFunctionExpr) {
                return;
            }

            // Only offer completions for standalone identifiers
            if (!(parent instanceof ElIdentifierExpr)) {
                return;
            }

            // Navigate from EL PSI up to the enclosing YAML element
            var yamlElement = PsiTreeUtil.getParentOfType(position, YAMLValue.class);
            if (yamlElement == null) {
                return;
            }

            var variables = VariablesProvider.getVariables(yamlElement);
            for (var variable : variables) {
                var schema = variable.schema();
                var lookup = new VariableLookup(variable.name(), variable.source(), schema);
                var builder = LookupElementBuilder.create(lookup, variable.name())
                        .withIcon(AllIcons.Nodes.Variable)
                        .withTailText("  " + variable.source().shortLabel(), true)
                        .withTypeText(SchemaType.displayName(schema.schemaType()));
                result.addElement(builder);
            }
        }
    }

    private static class ElPropertyCompletionProvider extends CompletionProvider<CompletionParameters> {

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters,
                                      @NotNull ProcessingContext context,
                                      @NotNull CompletionResultSet result) {
            var position = parameters.getPosition();
            var parent = position.getParent();

            if (!(parent instanceof ElMemberName memberName)) {
                return;
            }

            var objectSchema = ElAccessChainResolver.resolvePropertiesAtCaret(memberName);
            if (objectSchema == null) {
                return;
            }

            for (var entry : objectSchema.properties().entrySet()) {
                var prop = entry.getValue();
                var lookup = new PropertyLookup(entry.getKey(), prop);
                var builder = LookupElementBuilder.create(lookup, entry.getKey())
                        .withIcon(AllIcons.Nodes.Property)
                        .withTypeText(SchemaType.displayName(prop.schemaType()));

                result.addElement(builder);
            }
        }
    }
}
