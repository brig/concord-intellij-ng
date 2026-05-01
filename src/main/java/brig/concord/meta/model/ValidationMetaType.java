// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model;

import brig.concord.ConcordBundle;
import brig.concord.highlighting.ConcordHighlightingColors;
import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.HighlightProvider;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlEnumType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static brig.concord.yaml.meta.model.TypeProps.descKey;

public class ValidationMetaType extends ConcordMetaType implements HighlightProvider {

    private static final ValidationMetaType INSTANCE = new ValidationMetaType();

    private static final Map<String, YamlMetaType> features = Map.of(
            "taskCalls", TaskCallValidationMetaType.getInstance()
    );

    public static ValidationMetaType getInstance() {
        return INSTANCE;
    }

    private ValidationMetaType() {
        super(descKey("doc.configuration.validation.description"));
    }

    @Override
    protected @NotNull Map<String, YamlMetaType> getFeatures() {
        return features;
    }

    @Override
    public @Nullable TextAttributesKey getKeyHighlight(String key) {
        return ConcordHighlightingColors.DSL_KEY;
    }

    @Override
    public @Nullable String getDocumentationExample() {
        return """
                configuration:
                  validation:
                    taskCalls:
                      in: fail
                      out: warn
                """;
    }

    private static class TaskCallValidationMetaType extends ConcordMetaType implements HighlightProvider {

        private static final TaskCallValidationMetaType INSTANCE = new TaskCallValidationMetaType();

        private static final Map<String, YamlMetaType> features = Map.of(
                "in", new ValidationModeMetaType(descKey("doc.configuration.validation.taskCalls.in.description")),
                "out", new ValidationModeMetaType(descKey("doc.configuration.validation.taskCalls.out.description"))
        );

        public static TaskCallValidationMetaType getInstance() {
            return INSTANCE;
        }

        private TaskCallValidationMetaType() {
            super(descKey("doc.configuration.validation.taskCalls.description"));
        }

        @Override
        protected @NotNull Map<String, YamlMetaType> getFeatures() {
            return features;
        }

        @Override
        public @Nullable TextAttributesKey getKeyHighlight(String key) {
            return ConcordHighlightingColors.DSL_KEY;
        }
    }

    private static class ValidationModeMetaType extends YamlEnumType {

        private ValidationModeMetaType(@NotNull TypeProps props) {
            super("string", props,
                    List.of(
                            EnumValue.ofKey("disabled", "doc.configuration.validation.mode.disabled.description"),
                            EnumValue.ofKey("warn", "doc.configuration.validation.mode.warn.description"),
                            EnumValue.ofKey("fail", "doc.configuration.validation.mode.fail.description")));
        }

        @Override
        protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
            var text = scalarValue.getTextValue();
            if (text.isEmpty()) {
                return;
            }

            if (getLiteralsStream().noneMatch(literal -> literal.equalsIgnoreCase(text))) {
                holder.registerProblem(scalarValue,
                        ConcordBundle.message("YamlEnumType.validation.error.value.unknown", text),
                        ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        }
    }
}
