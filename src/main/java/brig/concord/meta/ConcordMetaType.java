package brig.concord.meta;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.Field;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class ConcordMetaType extends YamlMetaType {

    protected ConcordMetaType(@NonNls @NotNull String typeName) {
        super(typeName);
    }

    protected abstract Map<String, Supplier<YamlMetaType>> getFeatures();

    protected Set<String> getRequiredFields() {
        return Collections.emptySet();
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
                .map(this::findFeatureByName)
                .collect(Collectors.toList());
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        YamlMetaType metaType = Optional.ofNullable(getFeatures())
                .map(map -> map.get(name))
                .map(Supplier::get)
                .orElse(null);
        if (metaType == null) {
            return null;
        }
        return new Field(name, metaType);
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
