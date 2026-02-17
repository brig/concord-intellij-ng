// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.meta.model;

import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.ConcordBundle;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLSequence;
import brig.concord.yaml.psi.YAMLValue;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class YamlArrayType extends YamlMetaType {

    private final @Nullable YamlMetaType myElementType;

    public YamlArrayType(@NotNull YamlMetaType elementType) {
        super(elementType.getTypeName("[]"));
        myElementType = elementType;
    }

    public YamlArrayType(@NotNull YamlMetaType elementType, @NotNull TypeProps props) {
        super(elementType.getTypeName("[]"), props);
        myElementType = elementType;
    }

    protected YamlArrayType(@NotNull String typeName) {
        super(typeName);
        myElementType = null;
    }

    protected YamlArrayType(@NotNull String typeName, @NotNull TypeProps props) {
        super(typeName, props);
        myElementType = null;
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        if(!(value instanceof YAMLSequence)) {
            problemsHolder.registerProblem(value, ConcordBundle.message("YamlUnknownValuesInspectionBase.error.array.is.required"));
        }
    }

    public @NotNull YamlMetaType getElementType() {
        return Objects.requireNonNull(myElementType, "Subclass must override getElementType()");
    }

    @Override
    public @Nullable Field findFeatureByName(@NotNull String name) {
        return null;
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

    }
}
