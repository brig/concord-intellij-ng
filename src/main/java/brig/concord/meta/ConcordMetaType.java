package brig.concord.meta;

import brig.concord.ConcordBundle;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.*;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLScalar;
import org.jetbrains.yaml.psi.YAMLSequenceItem;
import org.jetbrains.yaml.psi.YAMLValue;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public abstract class ConcordMetaType extends YamlMetaType {

    protected ConcordMetaType(@NonNls @NotNull String typeName) {
        super(typeName);
    }

    protected abstract Map<String, Supplier<YamlMetaType>> getFeatures();

    protected Set<String> getRequiredFields() {
        return Collections.emptySet();
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
        Map<String, Supplier<YamlMetaType>> features = getFeatures();
        if (features == null) {
            return Collections.emptyList();
        }
        return features.keySet()
                .stream()
                .sorted()
                .map(this::findFeatureByName)
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        YamlMetaType metaType = Optional.ofNullable(getFeatures())
                .map(map -> map.get(name))
                .map(Supplier::get)
                .orElse(null);
        return metaTypeToField(metaType, name);
    }

    protected Field metaTypeToField(YamlMetaType metaType, String name) {
        if (metaType == null) {
            return null;
        }

        if (!(metaType instanceof YamlAnyOfType)) {
            return new Field(name, metaType);
        }

        return new Field(name, metaType) {
            @Override
            public @NotNull Field resolveToSpecializedField(@NotNull YAMLValue element) {
                YamlMetaType result = metaType;

                YamlAnyOfType anyType = (YamlAnyOfType)metaType;
                if (element instanceof YAMLScalar) {
                    result = findScalarType(element, anyType);
                } else if (element instanceof YAMLSequenceItem) {
                    result = findArrayType(anyType);
                }
                return new Field(name, result != null ? result : metaType);
            }

            private static YamlMetaType findScalarType(YAMLValue element, YamlAnyOfType anyType) {
                YamlMetaType def = null;
                for (YamlMetaType t : anyType.getSubTypes()) {
                    if (t instanceof YamlIntegerType) {
                        if (element.getText().matches("[0-9]+")) {
                            return t;
                        }
                    } else if (t instanceof YamlScalarType) {
                        def = t;
                    }
                }
                return def;
            }

            private static YamlMetaType findArrayType(YamlAnyOfType anyType) {
                for (YamlMetaType t : anyType.getSubTypes()) {
                    if (t instanceof YamlArrayType) {
                        return t;
                    }
                }
                return null;
            }
        };
    }

    @Override
    public void buildInsertionSuffixMarkup(@NotNull YamlInsertionMarkup markup,
                                           Field.@NotNull Relation relation,
                                           ForcedCompletionPath.@NotNull Iteration iteration) {
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
