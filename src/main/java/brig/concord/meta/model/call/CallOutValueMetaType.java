package brig.concord.meta.model.call;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.meta.model.OutVarContainerMetaType;
import brig.concord.meta.model.value.StringMetaType;
import brig.concord.yaml.meta.model.CompletionContext;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public class CallOutValueMetaType extends StringMetaType implements OutVarContainerMetaType {

    public record OutParameterLookup(@NotNull String name, @NotNull String type, boolean mandatory,
                                     @Nullable String description) {
    }

    private static final CallOutValueMetaType INSTANCE = new CallOutValueMetaType();

    public static CallOutValueMetaType getInstance() {
        return INSTANCE;
    }

    private CallOutValueMetaType() {
        super("string");
    }

    @Override
    public @NotNull List<? extends LookupElement> getValueLookups(@NotNull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        var documentation = FlowCallParamsProvider.findFlowDocumentation(insertedScalar);
        if (documentation == null) {
            return List.of();
        }

        var outputParams = documentation.getOutputParameters();
        if (outputParams.isEmpty()) {
            return List.of();
        }

        return outputParams.stream()
                .map(param -> LookupElementBuilder.create(
                                new OutParameterLookup(param.getName(), param.getType(),
                                        param.isMandatory(), param.getDescription()),
                                param.getName())
                        .withTypeText(param.getType()))
                .collect(Collectors.toList());
    }
}
