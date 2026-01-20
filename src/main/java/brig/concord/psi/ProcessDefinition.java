package brig.concord.psi;

import brig.concord.navigation.FlowNamesIndex;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.*;

import java.util.*;

public class ProcessDefinition {

    private final YAMLDocument currentDocument;

    public ProcessDefinition(YAMLDocument currentDocument) {
        this.currentDocument = currentDocument;
    }

    @Nullable
    public PsiElement flow(String name) {
        var project = currentDocument.getProject();
        if (ActionUtil.isDumbMode(project)) {
            return null;
        }

        var scope = ConcordScopeService.getInstance(project).createSearchScope(currentDocument);

        var files = new CommonProcessors.CollectProcessor<VirtualFile>();
        FileBasedIndex.getInstance().getFilesWithKey(FlowNamesIndex.KEY,
                Collections.singleton(name),
                files,
                scope);

        for (var file : files.getResults()) {
            var psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile == null) {
                continue;
            }

            var doc = PsiTreeUtil.getChildOfType(psiFile, YAMLDocument.class);
            var result = flow(doc, name);
            if (result != null) {
                return result.getParent();
            }
        }

        return null;
    }

    public Set<String> flowNames() {
        Project project = currentDocument.getProject();
        if (ActionUtil.isDumbMode(project)) {
            return Collections.emptySet();
        }

        var scope = ConcordScopeService.getInstance(project).createSearchScope(currentDocument);

        return ApplicationManager.getApplication().runReadAction((Computable<Set<String>>) () -> {
            Set<String> result = new HashSet<>();
            FileBasedIndex.getInstance().processAllKeys(FlowNamesIndex.KEY, key -> {
                result.add(key);
                return true;
            }, scope, null);
            return result;
        });
    }

    private static PsiElement flow(PsiElement root, String name) {
        return YamlPsiUtils.get(root, YAMLPsiElement.class, "flows", name);
    }

//    private List<YAMLDocument> allDefinitions() {
//        List<PathMatcher> resources = resourcePatterns("concord");
//        return Stream.concat(
//                        FileUtils.findFiles(rootDoc.getProject(), resources).stream() // TODO: sort
//                                .map(e -> PsiTreeUtil.getChildOfType(e, YAMLDocument.class))
//                                .filter(Objects::nonNull),
//                        Stream.of(rootDoc))
//                .collect(Collectors.toList());
//    }

}
