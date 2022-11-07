package brig.concord.psi;

import com.intellij.psi.PsiFile;

import java.util.Set;

public interface ConcordFile extends PsiFile {

    Set<String> PROJECT_ROOT_FILE_NAMES = Set.of(
            ".concord.yml",
            "concord.yml",
            ".concord.yaml",
            "concord.yaml"
    );

    static boolean isRootFileName(String name) {
        return PROJECT_ROOT_FILE_NAMES.contains(name);
    }
}
