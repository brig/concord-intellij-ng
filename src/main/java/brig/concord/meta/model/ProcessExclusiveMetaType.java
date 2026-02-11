package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.documentation.Documented;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProcessExclusiveMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ProcessExclusiveMetaType INSTANCE = new ProcessExclusiveMetaType();

    private static final Set<String> requiredFeatures = Set.of("group");

    private static final Map<String, YamlMetaType> features = Map.of(
            "group", doc(new GroupMetaType(), "doc.configuration.exclusive.group"),
            "mode", doc(new ModeType(), "doc.configuration.exclusive.mode")
    );

    public static ProcessExclusiveMetaType getInstance() {
        return INSTANCE;
    }

    protected ProcessExclusiveMetaType() {
        setDocBundlePrefix("doc.configuration.exclusive");
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
    public @NotNull List<Documented.DocumentedField> getDocumentationFields() {
        var prefix = getDocBundlePrefix();
        var required = getRequiredFields();

        var groupDesc = ConcordBundle.findMessage(prefix + ".group.description");
        var modeDesc = ConcordBundle.findMessage(prefix + ".mode.description");

        var modeChildren = List.of(
                new Documented.DocumentedField("cancel", null, false,
                        ConcordBundle.findMessage(prefix + ".mode.cancel"), List.of()),
                new Documented.DocumentedField("cancelOld", null, false,
                        ConcordBundle.findMessage(prefix + ".mode.cancelOld"), List.of()),
                new Documented.DocumentedField("wait", null, false,
                        ConcordBundle.findMessage(prefix + ".mode.wait"), List.of())
        );

        return List.of(
                new Documented.DocumentedField("group",
                        features.get("group").getTypeName(),
                        required.contains("group"),
                        groupDesc),
                new Documented.DocumentedField("mode",
                        features.get("mode").getTypeName(),
                        required.contains("mode"),
                        modeDesc,
                        modeChildren)
        );
    }

    @Override
    public @Nullable String getDocumentationExample() {
        return "configuration:\n  exclusive:\n    group: \"myGroup\"\n    mode: \"cancel\"\n";
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    private static <T extends YamlMetaType> T doc(T type, String prefix) {
        type.setDocBundlePrefix(prefix);
        return type;
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
            super("Mode");
            withLiterals("cancel", "cancelOld", "wait");
        }
    }
}
