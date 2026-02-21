// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.meta.model.value;

import brig.concord.ConcordBundle;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.format.DateTimeParseException;

public class DurationMetaType extends StringMetaType {

    private static final DurationMetaType INSTANCE = new DurationMetaType();

    public static DurationMetaType getInstance() {
        return INSTANCE;
    }

    public DurationMetaType() {
    }

    public DurationMetaType(@NotNull TypeProps props) {
        super(props);
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        super.validateScalarValue(scalarValue, holder);

        try {
            Duration.parse(scalarValue.getTextValue());
        } catch (DateTimeParseException e) {
            holder.registerProblem(scalarValue, ConcordBundle.message("DurationType.error.scalar.value"), ProblemHighlightType.ERROR);
        }
    }
}
