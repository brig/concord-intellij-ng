// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.highlighting;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Replaces raw EL parser error messages (long token lists) with human-readable summaries.
 * Works together with {@link ElHighlightErrorFilter} which suppresses the default error rendering.
 */
public class ElErrorAnnotator implements Annotator {

    private static final Pattern EXPECTED_GOT_PATTERN =
            Pattern.compile("(.+) expected, got '(.+)'");

    private static final Set<String> OPERATORS = Set.of(
            "!=", "%", "&&", "*", "+", "+=", "->", "/",
            "<", "<=", "=", "==", ">", ">=", "?", "||",
            "and", "div", "eq", "ge", "gt", "instanceof",
            "le", "lt", "mod", "ne", "or"
    );

    private static final Set<String> EXPRESSION_TOKENS = Set.of(
            "IDENTIFIER", "INTEGER_LITERAL", "FLOAT_LITERAL",
            "SINGLE_QUOTED_STRING", "DOUBLE_QUOTED_STRING",
            "true", "false", "null", "empty", "not", "!", "-",
            "(", "[", "{"
    );

    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!(element instanceof PsiErrorElement errorElement)) {
            return;
        }

        var message = simplifyElError(errorElement.getErrorDescription());

        holder.newAnnotation(HighlightSeverity.ERROR, message)
                .range(errorElement)
                .create();
    }

    static String simplifyElError(String errorDescription) {
        var matcher = EXPECTED_GOT_PATTERN.matcher(errorDescription);
        if (!matcher.matches()) {
            return errorDescription;
        }

        var expectedPart = matcher.group(1);
        var actual = matcher.group(2);

        var rawTokens = expectedPart.split(",\\s*");
        var tokens = new ArrayList<String>();
        for (var raw : rawTokens) {
            if (raw.contains(" or ")) {
                var parts = raw.split("\\s+or\\s+", 2);
                tokens.add(parts[0].trim());
                tokens.add(parts[1].trim());
            } else {
                tokens.add(raw.trim());
            }
        }

        int operatorCount = 0;
        int exprCount = 0;
        var specific = new ArrayList<String>();

        for (var token : tokens) {
            if (OPERATORS.contains(token)) {
                operatorCount++;
            } else if (EXPRESSION_TOKENS.contains(token)) {
                exprCount++;
            } else {
                specific.add("'" + token + "'");
            }
        }

        var parts = new ArrayList<String>();
        if (operatorCount > 0) {
            parts.add("an operator");
        }
        if (exprCount > 0) {
            parts.add("an expression");
        }
        parts.addAll(specific);

        if (parts.isEmpty()) {
            return "Unexpected token '" + actual + "'";
        }

        return String.join(", ", parts) + " expected, got '" + actual + "'";
    }
}