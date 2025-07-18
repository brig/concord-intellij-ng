package brig.concord.yaml.meta.model;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import brig.concord.ConcordBundle;
import brig.concord.yaml.psi.YAMLQuotedText;
import brig.concord.yaml.psi.YAMLScalar;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public class YamlBooleanType extends YamlEnumType {
    public static YamlBooleanType getSharedInstance() {
        return StandardYamlBoolean.SHARED;
    }

    public YamlBooleanType(@NonNls @NotNull String name) {
        super(name);
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        if (scalarValue instanceof YAMLQuotedText) {
            //TODO: quickfix would be nice here
            holder.registerProblem(scalarValue,
                    ConcordBundle.message("YamlBooleanType.validation.error.quoted.value"),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            return;
        }

        super.validateScalarValue(scalarValue, holder);
    }

    private static class StandardYamlBoolean extends YamlBooleanType {
        private static final StandardYamlBoolean SHARED = new StandardYamlBoolean();

        StandardYamlBoolean() {
            super("yaml:boolean");
            setDisplayName("boolean");
            withLiterals("true", "false");
      /*
      Theoretically, YAML spec allows more exotic variants for boolean values, e.g "ON", "off", "No", or even "Y".
      Different consumers of YAML support different subsets of the accepted values, e.g Compose does not accept one-character variants,
      but, contrary to the spec, supports all-caps "YES" or mixed-caps "Off".
      */
            withHiddenLiterals(new LiteralBuilder()
                    .withLiteral("true", LiteralBuilder::CAPS, LiteralBuilder::First)
                    .withLiteral("false", LiteralBuilder::CAPS, LiteralBuilder::First)
                    .withAllCasesOf("on", "off", "yes", "no")
                    .toArray());
        }
    }

    public static class LiteralBuilder {
        private final Set<String> myResult = new LinkedHashSet<>();

        public LiteralBuilder withAllCasesOf(@NonNls String @NotNull ... literals) {
            for (String next : literals) {
                if (next != null) {
                    withLiteral(next, LiteralBuilder::lower, LiteralBuilder::CAPS, LiteralBuilder::First);
                }
            }
            return this;
        }

        public LiteralBuilder withLiteral(@NotNull String literal, Function<String, String>... capitalizations) {
            if (capitalizations.length == 0) {
                myResult.add(literal);
            }
            else {
                for (Function<String, String> next : capitalizations) {
                    myResult.add(next.apply(literal));
                }
            }
            return this;
        }

        public String[] toArray() {
            return ArrayUtilRt.toStringArray(myResult);
        }

        protected static @NotNull String lower(@NotNull String text) {
            return StringUtil.toLowerCase(text);
        }

        protected static @NotNull String CAPS(@NotNull String text) {
            return StringUtil.toUpperCase(text);
        }

        protected static @NotNull String First(@NotNull String text) {
            return StringUtil.toTitleCase(text);
        }
    }
}
