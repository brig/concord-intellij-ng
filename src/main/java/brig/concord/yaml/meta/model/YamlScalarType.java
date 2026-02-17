// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.meta.model;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.ConcordBundle;
import brig.concord.yaml.psi.YAMLCompoundValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLValue;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public abstract class YamlScalarType extends YamlMetaType {

    protected YamlScalarType(@NonNls @NotNull String typeName) {
        super(typeName);
    }

    protected YamlScalarType(@NonNls @NotNull String typeName, @NotNull TypeProps props) {
        super(typeName, props);
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
    public @NotNull Icon getIcon() {
        return PlatformIcons.PROPERTY_ICON;
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        if (value instanceof YAMLScalar) {
            validateScalarValue((YAMLScalar)value, problemsHolder);
        }
        else if (value instanceof YAMLCompoundValue) {
            problemsHolder.registerProblem(value, ConcordBundle.message("YamlScalarType.error.scalar.value"), ProblemHighlightType.ERROR);
        }
    }

    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        //
    }

    @Override
    public void buildInsertionSuffixMarkup(@NotNull YamlInsertionMarkup markup,
                                           @NotNull Field.Relation relation) {
        switch (relation) {
            case OBJECT_CONTENTS /* weird, but let's ignore and breakthrough to defaults */, SCALAR_VALUE -> {
                markup.append(": ");
                markup.appendCaret();
            }
            case SEQUENCE_ITEM -> {
                markup.append(":");
                markup.doTabbedBlockForSequenceItem(markup::appendCaret);
            }
            default -> throw new IllegalStateException("Unknown relation: " + relation); //NON-NLS
        }
    }
}
