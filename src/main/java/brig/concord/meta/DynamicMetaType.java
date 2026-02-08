package brig.concord.meta;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.YamlMetaType;

public interface DynamicMetaType {

    /**
     * Resolves the concrete meta type for the given PSI element.
     *
     * @return resolved type, or {@code null} if no resolution is possible
     */
    @Nullable YamlMetaType resolve(PsiElement element);
}
