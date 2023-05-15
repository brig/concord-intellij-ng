package brig.concord.meta.model;

import brig.concord.meta.ConcordMetaType;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLBundle;
import org.jetbrains.yaml.meta.model.YamlEnumType;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class TriggerExclusiveMetaType extends ConcordMetaType {

    public static class ModeType extends YamlEnumType {

        private static final ModeType INSTANCE = new ModeType();

        protected static ModeType getInstance() {
            return INSTANCE;
        }

        protected ModeType() {
            super("Mode");
            setDisplayName("[cancel|cancelOld|wait]");
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
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        if (value instanceof YAMLMapping m) {
            Set<String> keys = m.getKeyValues().stream()
                    .map(YAMLKeyValue::getKeyText)
                    .collect(Collectors.toSet());

            if (!keys.contains("group") && !keys.contains("groupBy")) {
                String msg = YAMLBundle.message("YamlMissingKeysInspectionBase.missing.keys", "group or groupBy");
                problemsHolder.registerProblem(m, msg, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

            }
        }

        super.validateValue(value, problemsHolder);
    }
}
