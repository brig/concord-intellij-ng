package brig.concord.psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.CollectionFactory;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class FileUtils {

    private FileUtils() {
    }

    @Nullable
    public static VirtualFile getRootYamlDir(Project project, VirtualFile file) {
        return ProjectRootManager.getInstance(project).getFileIndex().getContentRootForFile(file);
    }

    public static List<PsiFile> findFiles(Project project, List<PathMatcher> patterns) {

        Set<String> names = CollectionFactory.createSmallMemoryFootprintSet();
        FilenameIndex.processAllFileNames((String s) -> {
            for (String ext : ConcordFile.PROJECT_ROOT_FILE_NAMES) {
                if (s.endsWith(ext)) {
                    names.add(s);
                }
            }
            return true;
        }, GlobalSearchScope.projectScope(project), null);

        List<PsiFile> result = new ArrayList<>();
        FilenameIndex.processFilesByNames(names, false, GlobalSearchScope.projectScope(project), null, virtualFile -> {
            Path path = virtualFile.getFileSystem().getNioPath(virtualFile);
            if (path == null || path.toString().contains("/target/")) {
                return true;
            }

            if (patterns.stream().anyMatch(m -> m.matches(path))) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                if (psiFile != null) {
                    result.add(psiFile);
                }
            }
            return true;
        });


//        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(ConcordFileType.INSTANCE, GlobalSearchScope.projectScope(project));
//        for (VirtualFile virtualFile : virtualFiles) {
//            Path path = virtualFile.getFileSystem().getNioPath(virtualFile);
//            if (path == null || path.toString().contains("/target/")) {
//                continue;
//            }
//
//            if (patterns.stream().anyMatch(m -> m.matches(path))) {
//                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
//                if (psiFile != null) {
//                    result.add(psiFile);
//                }
//            }
//        }
        return result;
    }
}
