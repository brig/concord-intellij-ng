package brig.concord;

import brig.concord.psi.impl.yaml.YAMLConcordKeyValueImpl;
import brig.concord.psi.impl.yaml.YAMLConcordPlainTextImpl;
import brig.concord.psi.impl.yaml.YAMLConcordQuotedStringImpl;
import brig.concord.psi.impl.yaml.YAMLConcordScalarList;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLParserDefinition;

public class YamlConcordParserDefinition extends YAMLParserDefinition implements ParserDefinition {

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
            return new YAMLConcordQuotedStringImpl(node);
        }
        return super.createElement(node);
    }
}