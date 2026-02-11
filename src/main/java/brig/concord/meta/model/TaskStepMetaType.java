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
import java.util.Set;

public class TaskStepMetaType extends IdentityMetaType {

    private static final TaskStepMetaType INSTANCE = new TaskStepMetaType();

    public static TaskStepMetaType getInstance() {
        return INSTANCE;
    }

    // lazy init via holder to break circular static dependency through StepsMetaType
    private static class FeaturesHolder {
        static final Map<String, YamlMetaType> FEATURES = StepFeatures.combine(
                StepFeatures.NAME_AND_META, StepFeatures.ERROR, StepFeatures.LOOP_AND_RETRY,
                Map.of("task", TaskNameMetaType.getInstance(),
                       "in", TaskInParamsMetaType.getInstance(),
                       "out", TaskOutParamsMetaType.getInstance(),
                       "ignoreErrors", BooleanMetaType.getInstance())
        );
    }

    protected TaskStepMetaType() {
        super("task", Set.of("task"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return FeaturesHolder.FEATURES;
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
