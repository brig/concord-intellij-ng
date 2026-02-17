// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.psi.impl;

import brig.concord.lexer.ConcordElTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.psi.YAMLScalar;

import java.util.ArrayList;
import java.util.List;

public class YAMLPlainTextImpl extends YAMLBlockScalarImpl implements YAMLScalar {
    public YAMLPlainTextImpl(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    protected @NotNull IElementType getContentType() {
        return YAMLTokenTypes.TEXT;
    }

    @Override
    protected boolean getIncludeFirstLineInContent() {
        return true;
    }

    @Override
    public @NotNull YamlScalarTextEvaluator getTextEvaluator() {
        return new YamlScalarTextEvaluator<>(this) {

            @Override
            public @NotNull List<TextRange> getContentRanges() {
                final int myStart = getTextRange().getStartOffset();
                final List<TextRange> result = new ArrayList<>();

                boolean seenText = false;
                for (ASTNode child = getFirstContentNode(); child != null; child = child.getTreeNext()) {
                    IElementType ct = child.getElementType();
                    if (ct == YAMLTokenTypes.TEXT
                            || ct == ConcordElTokenTypes.EL_EXPR_START
                            || ct == ConcordElTokenTypes.EL_EXPR
                            || ct == ConcordElTokenTypes.EL_EXPR_BODY
                            || ct == ConcordElTokenTypes.EL_EXPR_END) {
                        seenText = true;
                        TextRange childRange = child.getTextRange().shiftRight(-myStart);
                        // Merge with previous range if adjacent (e.g. TEXT + EL_EXPR_START + EL_EXPR + EL_EXPR_END + TEXT)
                        if (!result.isEmpty()) {
                            TextRange last = result.getLast();
                            if (last.getEndOffset() == childRange.getStartOffset()) {
                                result.set(result.size() - 1, new TextRange(last.getStartOffset(), childRange.getEndOffset()));
                                continue;
                            }
                        }
                        result.add(childRange);
                    }
                    else if (ct == YAMLTokenTypes.EOL) {
                        if (!seenText) {
                            result.add(child.getTextRange().shiftRight(-myStart));
                        }
                        seenText = false;
                    }
                }

                return result;
            }

            @Override
            protected @NotNull String getRangesJoiner(@NotNull CharSequence text, @NotNull List<TextRange> contentRanges, int indexBefore) {
                if (isNewline(text, contentRanges.get(indexBefore)) || isNewline(text, contentRanges.get(indexBefore + 1))) {
                    return "";
                }
                else {
                    return " ";
                }
            }

            private static boolean isNewline(@NotNull CharSequence text, @NotNull TextRange range) {
                return range.getLength() == 1 && text.charAt(range.getStartOffset()) == '\n';
            }
        };
    }

    @Override
    public String toString() {
        return "YAML plain scalar text";
    }


    @Override
    public boolean isMultiline() {
        return getNode().findChildByType(YAMLTokenTypes.EOL) != null;
    }
}
