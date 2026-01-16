package brig.concord.parser;

import brig.concord.ConcordLanguage;
import brig.concord.lexer.ConcordYAMLFlexLexer;
import brig.concord.lexer.FlowDocElementTypes;
import brig.concord.psi.impl.ConcordFileImpl;
import brig.concord.psi.impl.FlowDocParameterImpl;
import brig.concord.psi.impl.FlowDocumentationImpl;
import brig.concord.psi.impl.yaml.YAMLConcordKeyValueImpl;
import brig.concord.psi.impl.yaml.YAMLConcordPlainTextImpl;
import brig.concord.psi.impl.yaml.YAMLConcordQuotedTextImpl;
import brig.concord.psi.impl.yaml.YAMLConcordScalarList;
import brig.concord.yaml.YAMLElementTypes;
import brig.concord.yaml.psi.impl.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;

import static brig.concord.yaml.YAMLElementTypes.*;
import static brig.concord.yaml.YAMLElementTypes.SCALAR_QUOTED_STRING;

public class ConcordYAMLParserDefinition implements ParserDefinition {

    public static final IFileElementType FILE = new IFileElementType(ConcordLanguage.INSTANCE);

    @Override
    public @NotNull Lexer createLexer(final Project project) {
        return new ConcordYAMLFlexLexer();
    }

    @Override
    public @NotNull PsiParser createParser(final Project project) {
        return new ConcordYAMLParser();
    }

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new ConcordFileImpl(viewProvider);
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE;
    }

    @Override
    public @NotNull TokenSet getWhitespaceTokens() {
        return YAMLElementTypes.WHITESPACE_TOKENS;
    }

    @Override
    public @NotNull TokenSet getCommentTokens() {
        return YAMLElementTypes.YAML_COMMENT_TOKENS;
    }

    @Override
    public @NotNull TokenSet getStringLiteralElements() {
        return YAMLElementTypes.TEXT_SCALAR_ITEMS;
    }

    @Override
    public @NotNull PsiElement createElement(final ASTNode node) {
        final IElementType type = node.getElementType();
        // Flow documentation elements
        if (type == FlowDocElementTypes.FLOW_DOCUMENTATION) {
            return new FlowDocumentationImpl(node);
        }
        if (type == FlowDocElementTypes.FLOW_DOC_PARAMETER) {
            return new FlowDocParameterImpl(node);
        }
        if (type == FlowDocElementTypes.FLOW_DOC_IN_SECTION ||
                type == FlowDocElementTypes.FLOW_DOC_OUT_SECTION ||
                type == FlowDocElementTypes.FLOW_DOC_DESCRIPTION) {
            return new ASTWrapperPsiElement(node);
        }

        if (type == YAMLElementTypes.DOCUMENT){
            return new YAMLDocumentImpl(node);
        }
        if (type == YAMLElementTypes.KEY_VALUE_PAIR) {
            return new YAMLConcordKeyValueImpl(node);
        }
        if (type == YAMLElementTypes.COMPOUND_VALUE) {
            return new YAMLCompoundValueImpl(node);
        }
        if (type == YAMLElementTypes.SEQUENCE) {
            return new YAMLBlockSequenceImpl(node);
        }
        if (type == YAMLElementTypes.MAPPING) {
            return new YAMLBlockMappingImpl(node);
        }
        if (type == YAMLElementTypes.SEQUENCE_ITEM) {
            return new YAMLSequenceItemImpl(node);
        }
        if (type == YAMLElementTypes.HASH) {
            return new YAMLHashImpl(node);
        }
        if (type == YAMLElementTypes.ARRAY) {
            return new YAMLArrayImpl(node);
        }
        if (type == YAMLElementTypes.SCALAR_LIST_VALUE) {
            return new YAMLConcordScalarList(node);
        }
        if (type == YAMLElementTypes.SCALAR_TEXT_VALUE) {
            return new YAMLScalarTextImpl(node);
        }
        if (type == YAMLElementTypes.SCALAR_PLAIN_VALUE) {
            return new YAMLConcordPlainTextImpl(node);
        }
        if (type == YAMLElementTypes.SCALAR_QUOTED_STRING) {
            return new YAMLConcordQuotedTextImpl(node);
        }
        if (type == YAMLElementTypes.ANCHOR_NODE) {
            return new YAMLAnchorImpl(node);
        }
        if (type == YAMLElementTypes.ALIAS_NODE) {
            return new YAMLAliasImpl(node);
        }
        return new YAMLPsiElementImpl(node);
    }

    @Override
    public @NotNull SpaceRequirements spaceExistenceTypeBetweenTokens(final ASTNode left, final ASTNode right) {
        return SpaceRequirements.MAY;
    }
}
