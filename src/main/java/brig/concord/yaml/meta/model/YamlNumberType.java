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

public class YamlNumberType extends YamlScalarType {
    private static final YamlNumberType SHARED_INSTANCE_NO_QUOTED_VALUES_ALLOWED = new YamlNumberType(false);
    private static final YamlNumberType SHARED_INSTANCE_QUOTED_VALUES_ALLOWED = new YamlNumberType(true);

    private final boolean myQuotedValuesAllowed;

    public static YamlNumberType getInstance(boolean quotedValuesAllowed) {
        return quotedValuesAllowed ? SHARED_INSTANCE_QUOTED_VALUES_ALLOWED : SHARED_INSTANCE_NO_QUOTED_VALUES_ALLOWED;
    }

    public YamlNumberType(boolean quotedValuesAllowed) {
        super("yaml:number", "number");
        myQuotedValuesAllowed = quotedValuesAllowed;
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        try {
            if (!myQuotedValuesAllowed && scalarValue instanceof YAMLQuotedText) {
                throw new NumberFormatException("no quoted values allowed");
            }

            final String textValue = scalarValue.getTextValue();

            // Float.parseFloat() successfully parses values like " 1.0 ", i.e. starting or ending with spaces,
            // which is not valid for typed schema
            if (textValue.startsWith(" ") || textValue.endsWith(" ")) {
                throw new NumberFormatException("contains spaces");
            }
            Float.parseFloat(textValue);
        }
        catch (NumberFormatException e) {
            holder.registerProblem(scalarValue, ConcordBundle.message("YamlNumberType.error.numeric.value"), ProblemHighlightType.ERROR);
        }
    }
}
