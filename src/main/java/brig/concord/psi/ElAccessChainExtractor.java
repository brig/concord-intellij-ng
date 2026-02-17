package brig.concord.psi;

import brig.concord.el.psi.*;
import brig.concord.lexer.ConcordElTokenTypes;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ElAccessChainExtractor {

    private ElAccessChainExtractor() {}

    /**
     * Extracts the full access chain from a pure EL expression inside a YAML scalar.
     * <p>
     * For a scalar {@code "${initiator}"}, returns {@code ["initiator"]}.
     * For {@code "${obj.inner}"}, returns {@code ["obj", "inner"]}.
     * Returns {@code null} if the scalar contains mixed text content,
     * method calls, non-string bracket access, or a non-identifier base.
     */
    public static @Nullable List<String> extractFullChain(@NotNull PsiElement scalar) {
        if (!isPureElExpression(scalar)) {
            return null;
        }

        var identifierExpr = PsiTreeUtil.findChildOfType(scalar, ElIdentifierExpr.class);
        if (identifierExpr == null) {
            return null;
        }

        // Simple identifier: ${initiator}
        if (!(identifierExpr.getParent() instanceof ElAccessExpr accessExpr)) {
            var segments = new ArrayList<String>();
            segments.add(identifierExpr.getIdentifier().getText());
            return segments;
        }

        // Must be the base of the access expression
        if (accessExpr.getExpression() != identifierExpr) {
            return null;
        }

        var segments = new ArrayList<String>();
        segments.add(identifierExpr.getIdentifier().getText());
        return collectSuffixSegments(segments, accessExpr.getSuffixList(), null);
    }

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

        var result = collectSuffixSegments(segments, accessExpr.getSuffixList(), currentDotSuffix);
        if (result == null) {
            return List.of();
        }
        return result;
    }

    /**
     * Walks through suffixes, collecting property segments.
     * Stops before {@code stopBefore} if non-null; collects all suffixes if null.
     * Returns {@code null} if the chain is broken by a method call, non-string bracket, etc.
     */
    private static @Nullable List<String> collectSuffixSegments(@NotNull ArrayList<String> segments,
                                                                 @NotNull List<ElSuffix> suffixes,
                                                                 @Nullable ElDotSuffix stopBefore) {
        for (var suffix : suffixes) {
            if (suffix == stopBefore) {
                break;
            }

            if (suffix instanceof ElDotSuffix dotSuffix) {
                var mn = dotSuffix.getMemberName();
                if (mn == null) {
                    return null;
                }
                // Method calls break the property chain
                if (dotSuffix.getArgList() != null) {
                    return null;
                }
                segments.add(mn.getText());
            } else if (suffix instanceof ElBracketSuffix bracketSuffix) {
                // obj["prop"] is equivalent to obj.prop
                var propName = extractStringLiteralValue(bracketSuffix.getExpression());
                if (propName == null) {
                    return null;
                }
                // bracket with argList (e.g. obj["m"](args)) breaks the chain
                if (bracketSuffix.getArgList() != null) {
                    return null;
                }
                segments.add(propName);
            } else {
                // CallSuffix or unknown — breaks the chain
                return null;
            }
        }

        return segments;
    }

    /**
     * Checks whether the scalar is a pure EL expression (only ${...} with no surrounding text).
     * For quoted scalars, SCALAR_DSTRING/SCALAR_STRING tokens of length 1 are quote delimiters.
     * Any longer scalar token means mixed text content.
     */
    private static boolean isPureElExpression(@NotNull PsiElement scalar) {
        var node = scalar.getNode();
        if (node == null) {
            return false;
        }

        boolean hasElExpr = false;
        for (ASTNode child = node.getFirstChildNode(); child != null; child = child.getTreeNext()) {
            var type = child.getElementType();
            if (type == ConcordElTokenTypes.EL_EXPR_START || type == ConcordElTokenTypes.EL_EXPR_END) {
                continue;
            }
            if (type == ConcordElTokenTypes.EL_EXPR) {
                hasElExpr = true;
                continue;
            }
            if (type == YAMLTokenTypes.TEXT) {
                if (!child.getText().isBlank()) {
                    return false;
                }
            } else if (type == YAMLTokenTypes.SCALAR_DSTRING || type == YAMLTokenTypes.SCALAR_STRING) {
                // Quote delimiters are exactly 1 char; anything longer is mixed text
                if (child.getTextLength() > 1) {
                    return false;
                }
            }
        }
        return hasElExpr;
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
