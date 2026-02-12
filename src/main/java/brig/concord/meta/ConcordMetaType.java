package brig.concord.meta;

import brig.concord.ConcordBundle;
import brig.concord.meta.model.value.AnyOfType;
import brig.concord.meta.model.value.BooleanMetaType;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.*;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLValue;

import brig.concord.documentation.Documented;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ConcordMetaType extends YamlMetaType {

    protected ConcordMetaType() {
        super("object");
    }

    protected abstract @NotNull Map<String, YamlMetaType> getFeatures();

    protected Set<String> getRequiredFields() {
        return Collections.emptySet();
    }

    @Override
    public @NotNull List<Documented.DocumentedField> getDocumentationFields() {
        var required = getRequiredFields();
        return getFeatures().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    List<DocumentedField> children = List.of();
                    if (e.getValue() instanceof YamlEnumType enumType) {
                        var descriptions = enumType.getLiteralDescriptions();
                        if (descriptions.length > 0) {
                            children = new ArrayList<>(enumType.getLiterals().length);
                            String[] literals = enumType.getLiterals();
                            for (int i = 0; i < literals.length; i++) {
                                children.add(new DocumentedField(literals[i], enumType.getTypeName(), false, descriptions[i], List.of()));
                            }
                        }
                    }

                    var desc = e.getValue().getDescription();
                    return new Documented.DocumentedField(
                            e.getKey(),
                            e.getValue().getTypeName(),
                            required.contains(e.getKey()),
                            desc,
                            children);
                })
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
        return getRequiredFields().stream()
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
