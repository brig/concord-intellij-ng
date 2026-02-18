package brig.concord.completion;

import brig.concord.el.psi.ElFunctionExpr;
import brig.concord.el.psi.ElIdentifierExpr;
import brig.concord.el.psi.ElMemberName;
import brig.concord.el.psi.ElTypes;
import brig.concord.psi.ConcordBuiltInVars;
import brig.concord.psi.ConcordLoopVars;
import brig.concord.psi.VariablesProvider;
import brig.concord.psi.VariablesProvider.VariableSource;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.stream.Collectors;

public class ElCompletionContributor extends CompletionContributor {

    public ElCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(ElTypes.IDENTIFIER),
                new ElVariableCompletionProvider());
    }

    private static class ElVariableCompletionProvider extends CompletionProvider<CompletionParameters> {

        private static final Map<String, String> BUILT_IN_DESCRIPTIONS =
                ConcordBuiltInVars.VARS.stream()
                        .collect(Collectors.toMap(
                                ConcordBuiltInVars.BuiltInVar::name,
                                ConcordBuiltInVars.BuiltInVar::description));

        private static final Map<String, String> LOOP_VARS_DESCRIPTIONS =
                ConcordLoopVars.VARS.stream()
                        .collect(Collectors.toMap(ConcordLoopVars.LoopVar::name, ConcordLoopVars.LoopVar::description));

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
                var builder = LookupElementBuilder.create(variable.name())
                        .withTypeText(sourceLabel(variable.source()));

                if (variable.source() == VariableSource.BUILT_IN) {
                    var description = BUILT_IN_DESCRIPTIONS.get(variable.name());
                    if (description != null) {
                        builder = builder.withTailText("  " + description, true);
                    }
                }

                if (variable.source() == VariableSource.LOOP) {
                    var description = LOOP_VARS_DESCRIPTIONS.get(variable.name());
                    if (description != null) {
                        builder = builder.withTailText("  " + description, true);
                    }
                }

                result.addElement(builder);
            }
        }

        private static @NotNull String sourceLabel(@NotNull VariableSource source) {
            return switch (source) {
                case BUILT_IN -> "built-in";
                case ARGUMENT -> "argument";
                case FLOW_PARAMETER -> "flow in";
                case SET_STEP -> "set";
                case STEP_OUT -> "step out";
                case LOOP -> "loop";
            };
        }
    }
}
