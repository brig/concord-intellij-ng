package brig.concord;

import brig.concord.psi.impl.ConcordFileImpl;
import brig.concord.psi.impl.yaml.YAMLConcordKeyValueImpl;
import brig.concord.psi.impl.yaml.YAMLConcordPlainTextImpl;
import brig.concord.psi.impl.yaml.YAMLConcordQuotedTextImpl;
import brig.concord.psi.impl.yaml.YAMLConcordScalarList;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.YAMLParserDefinition;

import static brig.concord.yaml.YAMLElementTypes.*;

public class ConcordParserDefinition extends YAMLParserDefinition implements ParserDefinition {

    private static final IFileElementType FILE_ELEMENT_TYPE = new IFileElementType(ConcordLanguage.INSTANCE);

    @Override
    public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
        return new ConcordFileImpl(viewProvider);
    }

    @Override
    public @NotNull IFileElementType getFileNodeType() {
        return FILE_ELEMENT_TYPE;
    }

    @Override
    public @NotNull PsiElement createElement(ASTNode node) {
        IElementType type = node.getElementType();
        if (type == KEY_VALUE_PAIR) {
            return new YAMLConcordKeyValueImpl(node);
        } else if (type == SCALAR_PLAIN_VALUE) {
            return new YAMLConcordPlainTextImpl(node);
        } else if (type == SCALAR_LIST_VALUE) {
            return new YAMLConcordScalarList(node);
        } else if (type == SCALAR_QUOTED_STRING) {
            return new YAMLConcordQuotedTextImpl(node);
        }
        return super.createElement(node);
    }
}
