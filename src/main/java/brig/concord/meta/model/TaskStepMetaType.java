package brig.concord.meta.model;

import brig.concord.dependency.TaskRegistry;
import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.BooleanMetaType;
import brig.concord.meta.model.value.StringMetaType;
import brig.concord.schema.TaskInParamsMetaType;
import brig.concord.schema.TaskOutParamsMetaType;
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

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class TaskStepMetaType extends IdentityMetaType {

    private static final TaskStepMetaType INSTANCE = new TaskStepMetaType();

    public static TaskStepMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, YamlMetaType> features = StepFeatures.combine(
            StepFeatures.nameAndMeta(), StepFeatures.error(), StepFeatures.loopAndRetry(),
            Map.of("task", TaskNameMetaType.getInstance(),
                   "in", TaskInParamsMetaType.getInstance(),
                   "out", TaskOutParamsMetaType.getInstance(),
                   "ignoreErrors", new BooleanMetaType(descKey("doc.step.feature.ignoreErrors.description")))
    );

    private TaskStepMetaType() {
        super("task", descKey("doc.step.task.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    public static class TaskNameMetaType extends StringMetaType implements HighlightProvider {

        private static final TaskNameMetaType INSTANCE = new TaskNameMetaType();

        public static TaskNameMetaType getInstance() {
            return INSTANCE;
        }

        @Override
        public @Nullable TextAttributesKey getValueHighlight(String value) {
            return ConcordHighlightingColors.TARGET_IDENTIFIER;
        }

        private TaskNameMetaType() {
            super(descKey("doc.step.task.key.description").andRequired());
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
