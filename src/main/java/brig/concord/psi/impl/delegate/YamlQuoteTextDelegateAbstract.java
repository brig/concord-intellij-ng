package brig.concord.psi.impl.delegate;

import brig.concord.psi.impl.yaml.YAMLQuotedTextImpl_;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class YamlQuoteTextDelegateAbstract extends YAMLQuotedTextImpl_ {

    protected YamlQuoteTextDelegateAbstract(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return Optional.ofNullable(getReference())
                .stream()
                .toArray(PsiReference[]::new);
    }
}
