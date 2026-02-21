// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.lexer;

import brig.concord.el.ElLanguage;
import brig.concord.el.ElLexerAdapter;
import brig.concord.el.parser.ElParser;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Lazily-parsed element type for EL expression content (the part between ${ and }).
 * When the PSI tree for this node is needed, it is parsed using the EL parser/lexer.
 */
public class ConcordElExpressionElementType extends ILazyParseableElementType {

    public ConcordElExpressionElementType() {
        super("EL_EXPRESSION", ElLanguage.INSTANCE);
    }

    @Override
    public ASTNode createNode(@Nullable CharSequence text) {
        var node = new LazyParseableElement(this, text);
        node.putUserData(LANGUAGE_KEY, ElLanguage.INSTANCE);
        return node;
    }

    @Override
    protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull com.intellij.psi.PsiElement psi) {
        var project = psi.getProject();
        var text = chameleon.getChars();

        var builder = PsiBuilderFactory.getInstance().createBuilder(
                project,
                chameleon,
                new ElLexerAdapter(),
                ElLanguage.INSTANCE,
                text
        );

        var parser = new ElParser();
        parser.parseLight(this, builder);
        return builder.getTreeBuilt().getFirstChildNode();
    }
}
