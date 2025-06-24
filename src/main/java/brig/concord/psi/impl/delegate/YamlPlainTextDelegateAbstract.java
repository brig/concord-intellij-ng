package brig.concord.psi.impl.delegate;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import brig.concord.yaml.psi.impl.YAMLPlainTextImpl;

import java.util.Optional;

public abstract class YamlPlainTextDelegateAbstract extends YAMLPlainTextImpl {

    protected YamlPlainTextDelegateAbstract(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference @NotNull [] getReferences() {
        return Optional.ofNullable(getReference())
                .stream()
                .toArray(PsiReference[]::new);
    }
}
