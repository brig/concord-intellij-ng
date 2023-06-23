package brig.concord.meta.model.call;

import brig.concord.ConcordBundle;
import brig.concord.documentation.FlowDefinitionDocumentationParser;
import brig.concord.meta.model.StringMetaType;
import brig.concord.psi.CommentsProcessor;
import brig.concord.psi.ProcessDefinition;
import brig.concord.psi.ProcessDefinitionProvider;
import brig.concord.psi.ref.FlowDefinitionReference;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
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

        String maybeFlowName = value.getTextValue();
        if (containsExpression(maybeFlowName)) {
            return;
        }

        PsiReference[] flowRefs = value.getReferences();
        for (PsiReference ref : flowRefs) {
            if (ref instanceof FlowDefinitionReference fdr) {
                PsiElement definition = fdr.resolve();
                if (definition != null) {
                    return;
                }
            }
        }

        holder.registerProblem(value, ConcordBundle.message("CallStepMetaType.error.undefinedFlow"), ProblemHighlightType.ERROR);
    }

    private static ProcessDefinition processDefinition(YAMLValue value) {
        return ProcessDefinitionProvider.getInstance().get(value);
    }
}
