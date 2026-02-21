// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.el;

import brig.concord.el.parser.ElParser;
import brig.concord.el.psi.ElFile;
import brig.concord.el.psi.ElTypes;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

public class ElParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(ElLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(Project project) {
        return new ElLexerAdapter();
    }

    @Override
    public @NotNull PsiParser createParser(Project project) {
        return new ElParser();
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return TokenSet.create(TokenType.WHITE_SPACE);
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return TokenSet.EMPTY;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return ElTokenSets.STRINGS;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        if (node.getElementType() instanceof ILazyParseableElementType) {
            return new ASTWrapperPsiElement(node);
        }
        return ElTypes.Factory.createElement(node);
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new ElFile(viewProvider);
    }

    @Override
    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
