// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta;

import brig.concord.ConcordBundle;
import brig.concord.documentation.Documented;
import brig.concord.meta.model.value.AnyOfType;
import brig.concord.yaml.meta.model.*;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ConcordMetaType extends YamlMetaType {

    protected ConcordMetaType() {
        super("object");
    }

    protected ConcordMetaType(@NotNull TypeProps props) {
        super("object", props);
    }

    protected abstract @NotNull Map<String, YamlMetaType> getFeatures();

    @Override
    public @NotNull List<Documented.DocumentedField> getDocumentationFields() {
        return getFeatures().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> toDocumentedField(e.getKey(), e.getValue()))
                .toList();
    }

    private static DocumentedField toDocumentedField(String name, YamlMetaType type) {
        return new DocumentedField(name, type.getTypeName(), type.isRequired(), type.getDescription(), enumChildren(type));
    }

    private static List<DocumentedField> enumChildren(YamlMetaType type) {
        if (!(type instanceof YamlEnumType enumType)) {
            return List.of();
        }

        var enumValues = enumType.getEnumValues();
        if (enumValues.isEmpty() || enumValues.getFirst().description() == null) {
            return List.of();
        }

        return enumValues.stream()
                .map(v -> new DocumentedField(v.literal(), enumType.getTypeName(), false, v.description(), List.of()))
                .toList();
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        if (value instanceof YAMLScalar) {
            problemsHolder.registerProblem(value, ConcordBundle.message("ConcordMetaType.error.object.is.required"), ProblemHighlightType.ERROR);
        }
    }

    @Override
    public @NotNull List<String> computeMissingFields(@NotNull Set<String> existingFields) {
        return getFeatures().entrySet().stream()
                .filter(e -> e.getValue().isRequired())
                .map(Map.Entry::getKey)
                .filter(s -> !existingFields.contains(s))
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
        var features = getFeatures();
        return features.keySet()
                .stream()
                .sorted()
                .map(this::findFeatureByName)
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        var metaType = getFeatures().get(name);
        return metaTypeToField(metaType, name);
    }

    protected Field metaTypeToField(YamlMetaType metaType, String name) {
        if (metaType == null) {
            return null;
        }

        if (!(metaType instanceof YamlAnyOfType anyOfType)) {
            return new Field(name, metaType);
        }

        if (metaType instanceof AnyOfType) {
            var arrayType = AnyOfField.findArrayType(anyOfType);
            if (arrayType != null) {
                return new Field(name, metaType)
                        .withRelationSpecificType(Field.Relation.SEQUENCE_ITEM, arrayType.getElementType());
            }
            return new Field(name, metaType);
        }

        return new AnyOfField(name, anyOfType);
    }

    @Override
    public void buildInsertionSuffixMarkup(@NotNull YamlInsertionMarkup markup,
                                           Field.@NotNull Relation relation) {
        markup.append(":");
        if (relation == Field.Relation.SEQUENCE_ITEM) {
            markup.doTabbedBlockForSequenceItem();
        } else {
            markup.increaseTabs(1);
            markup.newLineAndTabs();
        }
        markup.appendCaret();
    }
}
