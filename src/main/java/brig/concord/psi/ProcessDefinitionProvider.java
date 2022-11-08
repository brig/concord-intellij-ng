package brig.concord.psi;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.AstLoadingFilter;
import org.jetbrains.yaml.psi.YAMLDocument;

import java.nio.file.Path;

public class ProcessDefinitionProvider {

    private static final ProcessDefinitionProvider INSTANCE = new ProcessDefinitionProvider();

    public static ProcessDefinitionProvider getInstance() {
        return INSTANCE;
    }

    public ProcessDefinition get(PsiElement element) {
        return AstLoadingFilter.disallowTreeLoading(() -> _get(element));
    }

    public ProcessDefinition _get(PsiElement element) {
        YAMLDocument currentDoc = YamlPsiUtils.getDocument(element);
        if (currentDoc == null) {
            return null;
        }

        VirtualFile rootFile = YamlPsiUtils.rootConcordYaml(element);
        if (rootFile == null) {
            return null;
        }

        PsiFile rootPsiFile = PsiManager.getInstance(element.getProject()).findFile(rootFile);
        YAMLDocument rootDoc = YamlPsiUtils.getDocument(rootPsiFile);

        if (rootDoc == null || rootDoc.getContainingFile() == null || rootDoc.getContainingFile().getVirtualFile() == null) {
            return null;
        }

        Path rootYamlPath = rootDoc.getContainingFile().getVirtualFile().toNioPath();
        return new ProcessDefinition(rootYamlPath, rootDoc);
    }
}
