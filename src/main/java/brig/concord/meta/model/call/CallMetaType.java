package brig.concord.meta.model.call;

import brig.concord.ConcordBundle;
import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.StringMetaType;
import brig.concord.psi.ProcessDefinition;
import brig.concord.psi.ProcessDefinitionProvider;
import brig.concord.psi.ref.FlowDefinitionReference;
import brig.concord.yaml.meta.model.CompletionContext;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

import static brig.concord.psi.YamlPsiUtils.isDynamicExpression;
import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class CallMetaType extends StringMetaType implements HighlightProvider {

    private static final CallMetaType INSTANCE = new CallMetaType();

    public static CallMetaType getInstance() {
        return INSTANCE;
    }

    public CallMetaType() {
        super(descKey("doc.step.call.key.description"));
    }

    public CallMetaType(@NotNull TypeProps props) {
        super(props);
    }

    @Override
    public @Nullable TextAttributesKey getValueHighlight(String value) {
        return ConcordHighlightingColors.TARGET_IDENTIFIER;
    }

    @Override
    public @NotNull List<? extends LookupElement> getValueLookups(@NotNull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        var processDefinition = ProcessDefinitionProvider.getInstance().get(insertedScalar);
        var currentFlow = ProcessDefinition.findEnclosingFlowDefinition(insertedScalar);
        return processDefinition.flowNames().stream()
                .filter(name -> currentFlow == null || !name.equals(currentFlow.getKeyText()))
                .sorted()
                .map(name -> LookupElementBuilder.create(name)
                        .withPresentableText(name))
                .collect(Collectors.toList());
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar value, @NotNull ProblemsHolder holder) {
        super.validateScalarValue(value, holder);

        if (isDynamicExpression(value)) {
            return;
        }

        var flowRefs = value.getReferences();
        for (var ref : flowRefs) {
            if (ref instanceof FlowDefinitionReference fdr) {
                if (fdr.multiResolve(false).length > 0) {
                    return;
                }
            }
        }

        holder.registerProblem(value, ConcordBundle.message("CallStepMetaType.error.undefinedFlow"), ProblemHighlightType.ERROR);
    }
}
