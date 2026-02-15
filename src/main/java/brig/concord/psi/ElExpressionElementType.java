package brig.concord.psi;

import brig.concord.el.ElLanguage;
import brig.concord.el.ElLexerAdapter;
import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.ILazyParseableElementType;
import org.jetbrains.annotations.NotNull;

public class ElExpressionElementType extends ILazyParseableElementType {

    public static final ElExpressionElementType INSTANCE = new ElExpressionElementType();

    private ElExpressionElementType() {
        super("EL_EXPRESSION", ElLanguage.INSTANCE);
    }

    @Override
    protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
        var project = psi.getProject();
        var chars = chameleon.getChars();
        var lexer = new ElLexerAdapter();
        var builder = PsiBuilderFactory.getInstance()
                .createBuilder(project, chameleon, lexer, ElLanguage.INSTANCE, chars);
        var root = builder.mark();
        while (!builder.eof()) {
            builder.advanceLexer();
        }
        root.done(this);
        return builder.getTreeBuilt().getFirstChildNode();
    }
}
