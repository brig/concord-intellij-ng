package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.meta.ConcordMetaType;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLScalar;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ProcessExclusiveMetaType extends ConcordMetaType {

    private static final ProcessExclusiveMetaType INSTANCE = new ProcessExclusiveMetaType();

    private static final Set<String> requiredFeatures = Set.of("group");

    private static final Map<String, Supplier<YamlMetaType>> features = Map.of(
            "group", GroupMetaType::getInstance,
            "mode", ModeType::getInstance
    );

    public static ProcessExclusiveMetaType getInstance() {
        return INSTANCE;
    }

    protected ProcessExclusiveMetaType() {
        super("Exclusive");
    }

    @Override
    protected Map<String, Supplier<YamlMetaType>> getFeatures() {
        return features;
    }

    @Override
    protected Set<String> getRequiredFields() {
        return requiredFeatures;
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
