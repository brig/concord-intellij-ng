package brig.concord.psi;

import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLFile;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class YamlPsiUtils {

    public static VirtualFile rootConcordYaml(PsiElement element) {
        if (element == null || element.getContainingFile() == null) {
            return null;
        }

        VirtualFile elementFile = element.getContainingFile().getVirtualFile();
        if (elementFile == null) {
            return null;
        }

        VirtualFile rootDir = FileUtils.getRootYamlDir(element.getProject(), elementFile);
        if (rootDir == null) {
            return null;
        }

        for (String fileName : ConcordFile.PROJECT_ROOT_FILE_NAMES) {
            VirtualFile root = VfsUtil.findRelativeFile(rootDir, fileName);
            if (root != null) {
                return root;
            }
        }
        return null;
    }

    public static YAMLDocument getDocument(PsiElement element) {
        if (element == null) {
            return null;
        }

        YAMLFile file = YamlPsiUtils.getParentOfType(element, YAMLFile.class, true);
        if (file == null) {
            return null;
        }

        List<YAMLDocument> rootDocs = file.getDocuments();
        if (rootDocs.isEmpty()) {
            return null;
        }

        return rootDocs.get(0);
    }

    public static <T extends PsiElement> T get(PsiElement root, Class<T> type, String... path) {
        if (root == null) {
            return null;
        }

        PsiElement current = root;
        for (String p : path) {
            YAMLMapping m = getChildOfType(current, YAMLMapping.class, true);
            if (m == null) {
                return null;
            }

            YAMLKeyValue kv = m.getKeyValueByKey(p);
            if (kv == null) {
                return null;
            }
            current = kv.getValue();
        }
        return getChildOfType(current, type, true);
    }

    @Nullable
    public static <T extends PsiElement> T getChildOfType(@Nullable PsiElement root, @NotNull Class<T> type, boolean includeMySelf) {
        if (root == null) {
            return null;
        }
        if (includeMySelf && type.isInstance(root)) {
            return type.cast(root);
        }
        return PsiTreeUtil.getChildOfType(root, type);
    }

    @Nullable
    public static <T extends PsiElement> T getParentOfType(@Nullable PsiElement element, @NotNull Class<T> type, boolean includeMySelf) {
        if (element == null) {
            return null;
        }

        if (includeMySelf && type.isInstance(element)) {
            return type.cast(element);
        }
        return PsiTreeUtil.getParentOfType(element, type);
    }

    public static Set<String> keys(YAMLMapping element) {
        if (element == null) {
            return Collections.emptySet();
        }

        return element.getKeyValues().stream()
                .map(k -> k.getKeyText().trim())
                .collect(Collectors.toSet());
    }

    private YamlPsiUtils() {
    }
}
