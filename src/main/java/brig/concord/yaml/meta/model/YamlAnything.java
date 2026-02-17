// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLMapping;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Allows any value: scalar, sequence, or mapping with arbitrary nesting.
 */
public class YamlAnything extends YamlMetaType {
    private static final YamlAnything ourInstance = new YamlAnything();

    private static final Field ourAnyField = new Field("<any-key>", ourInstance)
            .withAnyName()
            .withRelationSpecificType(Field.Relation.SEQUENCE_ITEM, ourInstance)
            .withRelationSpecificType(Field.Relation.SCALAR_VALUE, ourInstance)
            .withEmptyValueAllowed(true);

    public YamlAnything() {
        super("any");
    }

    public YamlAnything(@NotNull TypeProps props) {
        super("any", props);
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return ourAnyField;
    }

    @Override
    public @NotNull List<String> computeMissingFields(@NotNull Set<String> existingFields) {
        return Collections.emptyList();
    }

    @Override
    public @NotNull List<Field> computeKeyCompletions(@Nullable YAMLMapping existingMapping) {
        return Collections.emptyList();
    }

    @Override
    public void buildInsertionSuffixMarkup(@NotNull YamlInsertionMarkup markup,
                                           Field.@NotNull Relation relation) {
        markup.append(": ");
        markup.appendCaret();
    }

    public static YamlMetaType getInstance() {
        return ourInstance;
    }
}
