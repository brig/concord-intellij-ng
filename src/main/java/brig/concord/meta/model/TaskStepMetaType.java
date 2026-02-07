package brig.concord.meta.model;

import brig.concord.dependency.TaskRegistry;
import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import brig.concord.yaml.meta.model.CompletionContext;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class TaskStepMetaType extends IdentityMetaType {

    private static final TaskStepMetaType INSTANCE = new TaskStepMetaType();

    public static TaskStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "task", TaskNameMetaType::getInstance,
            "name", StepNameMetaType::getInstance,
            "in", InParamsMetaType::getInstance,
            "out", TaskOutParamsMetaType::getInstance,
            "ignoreErrors", BooleanMetaType::getInstance,
            "loop", LoopMetaType::getInstance,
            "retry", RetryMetaType::getInstance,
            "meta", StepMetaMetaType::getInstance,
            "error", StepsMetaType::getInstance
    );

    protected TaskStepMetaType() {
        super("Task", "task", Set.of("task"));
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    static class TaskNameMetaType extends StringMetaType implements HighlightProvider {

        private static final TaskNameMetaType INSTANCE = new TaskNameMetaType();

        public static TaskNameMetaType getInstance() {
            return INSTANCE;
        }

        @Override
        public @Nullable TextAttributesKey getValueHighlight(String value) {
            return ConcordHighlightingColors.TARGET_IDENTIFIER;
        }

        @Override
        public @NotNull List<? extends LookupElement> getValueLookups(@NotNull YAMLScalar insertedScalar,
                                                                      @Nullable CompletionContext completionContext) {
            var project = insertedScalar.getProject();
            var taskNames = TaskRegistry.getInstance(project).getTaskNames(insertedScalar);

            return taskNames.stream()
                    .sorted()
                    .map(name -> LookupElementBuilder.create(name)
                            .withPresentableText(name)
                            .withTypeText("task"))
                    .toList();
        }
    }
}
