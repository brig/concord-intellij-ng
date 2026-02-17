package brig.concord.psi;

import brig.concord.el.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ElAccessChainExtractor {

    private ElAccessChainExtractor() {}

    /**
     * Extracts the access chain segments preceding the given {@link ElMemberName}.
     * <p>
     * For {@code obj.p1.p2.<caret>}, returns {@code ["obj", "p1", "p2"]}.
     * Bracket access with string literals is treated as property access:
     * {@code obj["p1"].p2.<caret>} also returns {@code ["obj", "p1", "p2"]}.
     * Returns an empty list if the chain contains a non-identifier base,
     * non-string bracket access, or method calls before the current member.
     */
    public static @NotNull List<String> extractChainSegments(@NotNull ElMemberName memberName) {
        // memberName -> ElDotSuffix -> ElAccessExpr
        if (!(memberName.getParent() instanceof ElDotSuffix currentDotSuffix)) {
            return List.of();
        }
        if (!(currentDotSuffix.getParent() instanceof ElAccessExpr accessExpr)) {
            return List.of();
        }

        // The base expression must be a simple identifier
        var base = accessExpr.getExpression();
        if (!(base instanceof ElIdentifierExpr identifierExpr)) {
            return List.of();
        }

        var segments = new ArrayList<String>();
        segments.add(identifierExpr.getIdentifier().getText());

        // Walk through suffixes in order, stopping before the current one
        for (var suffix : accessExpr.getSuffixList()) {
            if (suffix == currentDotSuffix) {
                break;
            }

            if (suffix instanceof ElDotSuffix dotSuffix) {
                var mn = dotSuffix.getMemberName();
                if (mn == null) {
                    return List.of();
                }
                // Method calls break the property chain
                if (dotSuffix.getArgList() != null) {
                    return List.of();
                }
                segments.add(mn.getText());
            } else if (suffix instanceof ElBracketSuffix bracketSuffix) {
                // obj["prop"] is equivalent to obj.prop
                var propName = extractStringLiteralValue(bracketSuffix.getExpression());
                if (propName == null) {
                    return List.of();
                }
                // bracket with argList (e.g. obj["m"](args)) breaks the chain
                if (bracketSuffix.getArgList() != null) {
                    return List.of();
                }
                segments.add(propName);
            } else {
                // CallSuffix or unknown — breaks the chain
                return List.of();
            }
        }

        return segments;
    }

    /**
     * Extracts the unquoted string value from a string literal expression.
     * Returns {@code null} if the expression is not a string literal.
     */
    private static @Nullable String extractStringLiteralValue(@Nullable ElExpression expr) {
        if (!(expr instanceof ElStringLiteral stringLiteral)) {
            return null;
        }
        var text = stringLiteral.getText();
        if (text.length() < 2) {
            return null;
        }
        // Strip surrounding quotes (' or ")
        return text.substring(1, text.length() - 1);
    }
}
