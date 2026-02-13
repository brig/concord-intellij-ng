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
            "group", new GroupMetaType().withDescriptionKey("doc.configuration.exclusive.group.description"),
            "mode", new ModeType().withDescriptionKey("doc.configuration.exclusive.mode.description")
    );

    public static ProcessExclusiveMetaType getInstance() {
        return INSTANCE;
    }

    protected ProcessExclusiveMetaType() {
        setDescriptionKey("doc.configuration.exclusive.description");
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
    public @Nullable String getDocumentationExample() {
        return "configuration:\n  exclusive:\n    group: \"myGroup\"\n    mode: \"cancel\"\n";
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    private static class GroupMetaType extends StringMetaType {

        @Override
        protected void validateScalarValue(@NotNull YAMLScalar value, @NotNull ProblemsHolder holder) {
            super.validateScalarValue(value, holder);

            if (value.getTextValue().trim().isEmpty()) {
                holder.registerProblem(value, ConcordBundle.message("StringType.error.empty.scalar.value"), ProblemHighlightType.ERROR);
            }
        }
    }

    private static class ModeType extends YamlEnumType {

        protected ModeType() {
            super("string");

            setLiterals("cancel", "cancelOld", "wait");
            setDescriptionKeys(
                    "doc.configuration.exclusive.mode.cancel.description",
                    "doc.configuration.exclusive.mode.cancelOld.description",
                    "doc.configuration.exclusive.mode.wait.description"
                    );
        }
    }
}
