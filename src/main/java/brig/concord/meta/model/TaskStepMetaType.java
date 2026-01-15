package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.HighlightProvider;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.Nullable;

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
    }
}
