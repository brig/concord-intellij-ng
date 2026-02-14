package brig.concord.psi;

import com.intellij.psi.PsiElement;
import com.intellij.util.AstLoadingFilter;
import org.jetbrains.annotations.NotNull;

public class ProcessDefinitionProvider {

    private static final ProcessDefinitionProvider INSTANCE = new ProcessDefinitionProvider();

    public static ProcessDefinitionProvider getInstance() {
        return INSTANCE;
    }

    public @NotNull ProcessDefinition get(PsiElement element) {
        return AstLoadingFilter.disallowTreeLoading(() -> _get(element));
    }

    private ProcessDefinition _get(PsiElement element) {
        return new ProcessDefinition(element);
    }
}
