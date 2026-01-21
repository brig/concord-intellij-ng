package brig.concord.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class YamlPsiUtils {

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

        var keyValues = element.getKeyValues();
        if (keyValues.isEmpty()) {
            return Collections.emptySet();
        }

        Set<String> result = new HashSet<>(keyValues.size());
        for (var kv : keyValues) {
            result.add(kv.getKeyText().trim());
        }
        return result;
    }

    private YamlPsiUtils() {
    }
}
