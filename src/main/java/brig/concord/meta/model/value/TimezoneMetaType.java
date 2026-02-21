// SPDX-License-Identifier: Apache-2.0
package brig.concord.meta.model.value;

import brig.concord.ConcordBundle;
import brig.concord.yaml.meta.model.YamlStringType;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.TimeZone;

public class TimezoneMetaType extends YamlStringType {

    private static final TimezoneMetaType INSTANCE = new TimezoneMetaType();

    public static TimezoneMetaType getInstance() {
        return INSTANCE;
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        boolean valid = Arrays.asList(TimeZone.getAvailableIDs()).contains(scalarValue.getTextValue());
        if (valid) {
            return;
        }

        holder.registerProblem(scalarValue, ConcordBundle.message("TimezoneType.error.scalar.value"), ProblemHighlightType.ERROR);
    }
}
