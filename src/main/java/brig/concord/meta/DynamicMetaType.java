package brig.concord.meta;

import com.intellij.psi.PsiElement;
import brig.concord.yaml.meta.model.YamlMetaType;

public interface DynamicMetaType {

    YamlMetaType resolve(PsiElement element);
}
