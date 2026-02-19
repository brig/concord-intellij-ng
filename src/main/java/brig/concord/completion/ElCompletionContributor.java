package brig.concord.completion;

import brig.concord.el.psi.ElFunctionExpr;
import brig.concord.el.psi.ElIdentifierExpr;
import brig.concord.el.psi.ElMemberName;
import brig.concord.el.psi.ElTypes;
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
import org.jetbrains.annotations.Nullable;

public class ElCompletionContributor extends CompletionContributor {

    public record VariableLookup(@NotNull String name, @NotNull VariableSource source,
                                 @Nullable SchemaProperty schema) {
    }

    public ElCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(ElTypes.IDENTIFIER),
                new ElVariableCompletionProvider());
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
                        .withTailText("  " + variable.source().shortLabel(), true);

                if (schema != null) {
                    builder = builder.withTypeText(SchemaType.displayName(schema.schemaType()));
                }

                result.addElement(builder);
            }
        }
    }
}
