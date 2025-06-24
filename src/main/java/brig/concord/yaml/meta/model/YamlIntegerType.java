package brig.concord.yaml.meta.model;

/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import brig.concord.ConcordBundle;
import brig.concord.yaml.psi.YAMLQuotedText;
import brig.concord.yaml.psi.YAMLScalar;

public class YamlIntegerType extends YamlScalarType {
    private static final YamlIntegerType SHARED_INSTANCE_NO_QUOTED_VALUES_ALLOWED = new YamlIntegerType(false);
    private static final YamlIntegerType SHARED_INSTANCE_QUOTED_VALUES_ALLOWED = new YamlIntegerType(true);

    private final boolean myQuotedValuesAllowed;

    public static YamlIntegerType getInstance(boolean quotedValuesAllowed) {
        return quotedValuesAllowed ? SHARED_INSTANCE_QUOTED_VALUES_ALLOWED : SHARED_INSTANCE_NO_QUOTED_VALUES_ALLOWED;
    }

    public YamlIntegerType(boolean quotedValuesAllowed) {
        super("yaml:integer", "integer");
        myQuotedValuesAllowed = quotedValuesAllowed;
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        try {
            if (!myQuotedValuesAllowed && scalarValue instanceof YAMLQuotedText) {
                throw new NumberFormatException("no quoted values allowed");
            }
            Long.parseLong(scalarValue.getTextValue());
        }
        catch (NumberFormatException e) {
            holder.registerProblem(scalarValue, ConcordBundle.message("YamlIntegerType.error.integer.value"), ProblemHighlightType.ERROR);
        }
    }
}
