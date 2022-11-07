package brig.concord;

import brig.concord.psi.impl.yaml.YAMLConcordKeyValueImpl;
import brig.concord.psi.impl.yaml.YAMLConcordPlainTextImpl;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLFindUsagesProvider;

public class FindUsageProvider extends YAMLFindUsagesProvider implements FindUsagesProvider {

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return true;
    }

    @Override
    public @Nullable @NonNls String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public @Nls @NotNull String getType(@NotNull PsiElement element) {
        System.out.println("<<<< gettype");
        return super.getType(element);
    }

    @Override
    public @Nls @NotNull String getDescriptiveName(@NotNull PsiElement element) {
        System.out.println("<<<< getDescriptiveName");

//        if (element instanceof YAMLConcordKeyValueImpl) {
//            final YamlMetaType metaType = getMetaValueType(element);
//            if (metaType instanceof OMTModelItemMetaType) {
//                return metaType.getTypeName();
//            }
//            return ((YAMLKeyValue) element).getKeyText();
//        } else if (element instanceof YAMLConcordPlainTextImpl) {
//            final YamlMetaType metaType = getMetaType(element);
//            if (metaType instanceof OMTNamedVariableMetaType) {
//                return ((OMTNamedVariableMetaType) metaType).getName((YAMLValue) element);
//            }
//        }

        return super.getDescriptiveName(element);
    }

    @Override
    public @Nls @NotNull String getNodeText(@NotNull PsiElement element,
                                            boolean useFullName) {
        System.out.println("<<<< getNodeText");

        if (element instanceof YAMLConcordKeyValueImpl || element instanceof YAMLConcordPlainTextImpl) {
            return getDescriptiveName(element);
        }

        return super.getNodeText(element, useFullName);
    }

//        private YamlMetaType getMetaType(PsiElement element) {
//            return OMTMetaTypeProvider.getInstance(element.getProject()).getResolvedMetaType(element);
//        }
//
//        private YamlMetaType getMetaValueType(PsiElement element) {
//            if (element instanceof YAMLKeyValue) {
//                return OMTMetaTypeProvider.getInstance(element.getProject())
//                        .getResolvedKeyValueMetaTypeMeta((YAMLKeyValue) element);
//            }
//            return getMetaType(element);
//        }
}
