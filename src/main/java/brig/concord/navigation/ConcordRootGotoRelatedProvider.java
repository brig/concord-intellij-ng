package brig.concord.navigation;

import brig.concord.psi.ConcordFile;
import brig.concord.psi.ConcordRoot;
import brig.concord.psi.ConcordScopeService;
import com.intellij.navigation.GotoRelatedItem;
import com.intellij.navigation.GotoRelatedProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides navigation from any Concord file to its root concord.yaml file(s).
 * Accessible via Navigate → Related Symbol (Ctrl+Alt+Home on Mac).
 */
public class ConcordRootGotoRelatedProvider extends GotoRelatedProvider {

    private static final String GROUP_NAME = "Concord Root";

    @Override
    public @NotNull List<? extends GotoRelatedItem> getItems(@NotNull PsiElement psiElement) {
        PsiFile psiFile = psiElement.getContainingFile();
        if (!(psiFile instanceof ConcordFile)) {
            return List.of();
        }

        var virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) {
            return List.of();
        }

        if (ConcordFile.isRootFileName(virtualFile.getName())) {
            return List.of();
        }

        var service = ConcordScopeService.getInstance(psiElement.getProject());
        var scopes = service.getScopesForFile(virtualFile);

        if (scopes.isEmpty()) {
            return List.of();
        }

        List<GotoRelatedItem> result = new ArrayList<>();
        var psiManager = PsiManager.getInstance(psiElement.getProject());

        for (ConcordRoot root : scopes) {
            var rootPsiFile = psiManager.findFile(root.getRootFile());
            if (rootPsiFile != null) {
                result.add(new GotoRelatedItem(rootPsiFile, GROUP_NAME) {
                    @Override
                    public String getCustomName() {
                        return root.getScopeName();
                    }

                    @Override
                    public String getCustomContainerName() {
                        return root.getRootFile().getName();
                    }
                });
            }
        }

        return result;
    }
}
