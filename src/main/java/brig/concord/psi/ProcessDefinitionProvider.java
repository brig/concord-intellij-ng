package brig.concord.psi;

import com.intellij.psi.PsiElement;
import com.intellij.util.AstLoadingFilter;
import brig.concord.yaml.psi.YAMLDocument;

public class ProcessDefinitionProvider {

    private static final ProcessDefinitionProvider INSTANCE = new ProcessDefinitionProvider();

    public static ProcessDefinitionProvider getInstance() {
        return INSTANCE;
    }

    public ProcessDefinition get(PsiElement element) {
        return AstLoadingFilter.disallowTreeLoading(() -> _get(element));
    }

    private ProcessDefinition _get(PsiElement element) {
        YAMLDocument currentDoc = YamlPsiUtils.getDocument(element);
        if (currentDoc == null) {
            return null;
        }
        return new ProcessDefinition(currentDoc);

// TODO: find root, process resources configuration...
//        YAMLDocument rootDoc;
//
//        VirtualFile rootFile = YamlPsiUtils.rootConcordYaml(element);
//        if (rootFile != null) {
//            PsiFile rootPsiFile = PsiManager.getInstance(element.getProject()).findFile(rootFile);
//            rootDoc = YamlPsiUtils.getDocument(rootPsiFile);
//        } else {
//            rootDoc = currentDoc;
//        }
//
//        if (rootDoc == null || rootDoc.getContainingFile() == null || rootDoc.getContainingFile().getVirtualFile() == null) {
//            return null;
//        }
//
//        return new ProcessDefinition(rootDoc);
    }
}
