package brig.concord.meta;

import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public interface DynamicMetaType {

    /**
     * Resolves the concrete meta type for the given PSI element.
     *
     * @return resolved type, or {@code null} if no resolution is possible
     */
    @Nullable YamlMetaType resolve(PsiElement element);
}
