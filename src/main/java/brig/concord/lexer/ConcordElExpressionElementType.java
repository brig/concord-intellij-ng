// SPDX-License-Identifier: Apache-2.0
package brig.concord.lexer;

import brig.concord.el.ElLanguage;
import brig.concord.el.ElLexerAdapter;
import brig.concord.el.parser.ElParser;
import brig.concord.yaml.YAMLTokenTypes;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lexer.Lexer;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Lazily-parsed element type for EL expression content (the part between ${ and }).
 * When the PSI tree for this node is needed, it is parsed using the EL parser/lexer.
 * <p>
 * For expressions inside YAML double-quoted strings, a {@link YamlDQElLexer} is used
 * to decode YAML escape sequences before EL lexing.
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

        Lexer lexer = isYamlDoubleQuoted(chameleon) ? new YamlDQElLexer() : new ElLexerAdapter();

        var builder = PsiBuilderFactory.getInstance().createBuilder(
                project,
                chameleon,
                lexer,
                ElLanguage.INSTANCE,
                text
        );

        var parser = new ElParser();
        parser.parseLight(this, builder);
        return builder.getTreeBuilt().getFirstChildNode();
    }

    /**
     * Checks whether the chameleon node is inside a YAML double-quoted scalar.
     * The first child of the parent node (SCALAR_QUOTED_STRING) is SCALAR_DSTRING for DQ strings.
     */
    private static boolean isYamlDoubleQuoted(@NotNull ASTNode chameleon) {
        var parent = chameleon.getTreeParent();
        if (parent == null) {
            return false;
        }
        var firstChild = parent.getFirstChildNode();
        return firstChild != null && firstChild.getElementType() == YAMLTokenTypes.SCALAR_DSTRING;
    }
}
