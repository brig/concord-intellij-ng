package brig.concord.meta.model;

import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import brig.concord.ConcordBundle;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLValue;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class TriggerExclusiveMetaType extends ConcordMetaType implements HighlightProvider {

    public static class ModeType extends YamlEnumType {

        private static final ModeType INSTANCE = new ModeType();

        protected static ModeType getInstance() {
            return INSTANCE;
        }

        protected ModeType() {
            super("Mode", "[cancel|cancelOld|wait]");
            withLiterals("cancel", "cancelOld", "wait");
        }
    }

    private static final TriggerExclusiveMetaType INSTANCE = new TriggerExclusiveMetaType();

    public static TriggerExclusiveMetaType getInstance() {
        return INSTANCE;
    }

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "group", StringMetaType::getInstance,
            "groupBy", StringMetaType::getInstance,
            "mode", ModeType::getInstance
    );

    protected TriggerExclusiveMetaType() {
        super("Trigger Exclusive");
    }

    @Override
    protected @NotNull Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        if (value instanceof YAMLMapping m) {
            Set<String> keys = m.getKeyValues().stream()
                    .map(YAMLKeyValue::getKeyText)
                    .collect(Collectors.toSet());

            if (!keys.contains("group") && !keys.contains("groupBy")) {
                String msg = ConcordBundle.message("YamlMissingKeysInspectionBase.missing.keys", "group or groupBy");
                problemsHolder.registerProblem(m, msg, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

            }
        }

        super.validateValue(value, problemsHolder);
    }
}
