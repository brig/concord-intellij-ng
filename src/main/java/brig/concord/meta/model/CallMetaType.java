package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.psi.ProcessDefinition;
import brig.concord.psi.ProcessDefinitionProvider;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.CompletionContext;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static brig.concord.meta.model.ExpressionMetaType.containsExpression;

@SuppressWarnings("UnstableApiUsage")
public class CallMetaType extends StringMetaType {

    private static final CallMetaType INSTANCE = new CallMetaType();

    public static CallMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull List<? extends LookupElement> getValueLookups(@NotNull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        ProcessDefinition processDefinition = processDefinition(insertedScalar);
        if (processDefinition == null) {
            return Collections.emptyList();
        }

        return processDefinition.flowNames().stream()
                .sorted()
                .map(name -> LookupElementBuilder.create(name)
                        .withPresentableText(name))
                .collect(Collectors.toList());
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar value, @NotNull ProblemsHolder holder) {
        super.validateScalarValue(value, holder);

        String text = value.getTextValue();
        if (containsExpression(text)) {
            return;
        }

        ProcessDefinition processDefinition = processDefinition(value);
        if (processDefinition == null) {
            return;
        }

        if (processDefinition.flow(text) == null) {
            holder.registerProblem(value, ConcordBundle.message("CallStepMetaType.error.undefinedFlow"), ProblemHighlightType.ERROR);
        }
    }

    private static ProcessDefinition processDefinition(YAMLValue value) {
        return ProcessDefinitionProvider.getInstance().get(value);
    }
}
