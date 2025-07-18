package brig.concord.yaml.psi.impl;

// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.YAMLElementTypes;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.psi.YAMLBlockScalar;
import brig.concord.yaml.psi.YAMLScalarList;
import brig.concord.yaml.psi.YamlPsiElementVisitor;

import java.util.List;

import static brig.concord.yaml.psi.impl.YAMLBlockScalarImplKt.isEol;

/**
 * @author oleg
 * @see <a href="https://yaml.org/spec/1.2-old/spec.html#id2795688">YAML spec, 8.1.2</a>
 */
public class YAMLScalarListImpl extends YAMLBlockScalarImpl implements YAMLScalarList, YAMLBlockScalar {
    public YAMLScalarListImpl(final @NotNull ASTNode node) {
        super(node);
    }

    @Override
    protected @NotNull IElementType getContentType() {
        return YAMLTokenTypes.SCALAR_LIST;
    }

    @Override
    public @NotNull YamlScalarTextEvaluator<YAMLScalarListImpl> getTextEvaluator() {
        return new YAMLBlockScalarTextEvaluator<>(this) {

            @Override
            protected @NotNull String getRangesJoiner(@NotNull CharSequence text, @NotNull List<TextRange> contentRanges, int indexBefore) {
                return "";
            }

            @Override
            public @NotNull String getTextValue(@Nullable TextRange rangeInHost) {
                String value = super.getTextValue(rangeInHost);
                if (!StringUtil.isEmptyOrSpaces(value) && getChompingIndicator() == ChompingIndicator.KEEP && isEnding(rangeInHost)) {
                    value += "\n";
                }
                return value;
            }

            @Override
            protected boolean shouldIncludeEolInRange(ASTNode child) {
                if (getChompingIndicator() == ChompingIndicator.KEEP) return true;

                if (isEol(child) &&
                        isEolOrNull(child.getTreeNext()) &&
                        !(YAMLTokenTypes.INDENT.equals(ObjectUtils.doIfNotNull(child.getTreePrev(), ASTNode::getElementType)) &&
                                myHost.getLinesNodes().size() <= 2)) {
                    return false;
                }

                ASTNode next = TreeUtil.findSibling(child.getTreeNext(), NON_SPACE_VALUES);
                if (isEol(next) &&
                        isEolOrNull(TreeUtil.findSibling(next.getTreeNext(), NON_SPACE_VALUES)) &&
                        getChompingIndicator() == ChompingIndicator.STRIP) {
                    return false;
                }

                return true;
            }

            private final TokenSet NON_SPACE_VALUES = TokenSet.orSet(YAMLElementTypes.SCALAR_VALUES, YAMLElementTypes.EOL_ELEMENTS);
        };
    }


    @Override
    public PsiLanguageInjectionHost updateText(@NotNull String text) {
        String original = getNode().getText();
        int commonPrefixLength = StringUtil.commonPrefixLength(original, text);
        int commonSuffixLength = StringUtil.commonSuffixLength(original, text);
        int indent = locateIndent();

        ASTNode scalarEol = getNode().findChildByType(YAMLTokenTypes.SCALAR_EOL);
        if (scalarEol == null) {
            // a very strange situation
            return super.updateText(text);
        }

        int eolOffsetInParent = scalarEol.getStartOffsetInParent();

        int startContent = eolOffsetInParent + indent + 1;
        if (startContent > commonPrefixLength) {
            // a very strange situation
            return super.updateText(text);
        }

        String originalRowPrefix = original.substring(startContent, commonPrefixLength);
        String indentString = StringUtil.repeatSymbol(' ', indent);

        String prefix = originalRowPrefix.replaceAll("\n" + indentString, "\n");
        String suffix = text.substring(text.length() - commonSuffixLength).replaceAll("\n" + indentString, "\n");

        String result = prefix + text.substring(commonPrefixLength, text.length() - commonSuffixLength) + suffix;
        return super.updateText(result);
    }

    @Override
    public String toString() {
        return "YAML scalar list";
    }

    @Override
    public void accept(@NotNull PsiElementVisitor visitor) {
        if (visitor instanceof YamlPsiElementVisitor) {
            ((YamlPsiElementVisitor)visitor).visitScalarList(this);
        }
        else {
            super.accept(visitor);
        }
    }
}
