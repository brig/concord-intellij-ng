package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.meta.model.value.StringMetaType;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ProcessExclusiveMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ProcessExclusiveMetaType INSTANCE = new ProcessExclusiveMetaType();

    private static final Map<String, YamlMetaType> features = Map.of(
            "group", new GroupMetaType(descKey("doc.configuration.exclusive.group.description").andRequired()),
            "mode", new ModeType(descKey("doc.configuration.exclusive.mode.description"))
    );

    public static ProcessExclusiveMetaType getInstance() {
        return INSTANCE;
    }

    private ProcessExclusiveMetaType() {
        super(descKey("doc.configuration.exclusive.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable String getDocumentationExample() {
        return """
                configuration:
                  exclusive:
                    group: "myGroup"
                    mode: "cancel"
                """;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    private static class GroupMetaType extends StringMetaType {

        private GroupMetaType(@NotNull TypeProps props) {
            super(props);
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

        private ModeType(@NotNull TypeProps props) {
            super("string", props);

            setLiterals("cancel", "cancelOld", "wait");
            setDescriptionKeys(
                    "doc.configuration.exclusive.mode.cancel.description",
                    "doc.configuration.exclusive.mode.cancelOld.description",
                    "doc.configuration.exclusive.mode.wait.description"
                    );
        }
    }
}
