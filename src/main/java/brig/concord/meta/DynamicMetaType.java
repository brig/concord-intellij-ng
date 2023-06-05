package brig.concord.meta;

import com.intellij.psi.PsiElement;
import org.jetbrains.yaml.meta.model.YamlMetaType;

public interface DynamicMetaType {

    YamlMetaType resolve(PsiElement element);
}
