package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.StringMetaType;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLScalar;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class ProcessExclusiveMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ProcessExclusiveMetaType INSTANCE = new ProcessExclusiveMetaType();

    private static final Set<String> requiredFeatures = Set.of("group");

    private static final Map<String, YamlMetaType> features = Map.of(
            "group", GroupMetaType.getInstance(),
            "mode", ModeType.getInstance()
    );

    public static ProcessExclusiveMetaType getInstance() {
        return INSTANCE;
    }

    protected ProcessExclusiveMetaType() {
        super("Exclusive");
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return requiredFeatures;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    private static class GroupMetaType extends StringMetaType {

        private static final GroupMetaType INSTANCE = new GroupMetaType();

        public static GroupMetaType getInstance() {
            return INSTANCE;
        }

        @Override
        protected void validateScalarValue(@NotNull YAMLScalar value, @NotNull ProblemsHolder holder) {
            super.validateScalarValue(value, holder);

            if (value.getTextValue().trim().isEmpty()) {
                holder.registerProblem(value, ConcordBundle.message("StringType.error.empty.scalar.value"), ProblemHighlightType.ERROR);
            }
        }
    }

    private static class ModeType extends YamlEnumType {

        private static final ModeType INSTANCE = new ModeType();

        public static ModeType getInstance() {
            return INSTANCE;
        }

        protected ModeType() {
            super("Mode", "[cancel|cancelOld|wait]");
            withLiterals("cancel", "cancelOld", "wait");
        }
    }
}
