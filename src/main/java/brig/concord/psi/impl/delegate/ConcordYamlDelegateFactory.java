package brig.concord.psi.impl.delegate;

import brig.concord.meta.ConcordMetaTypeProvider;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.meta.model.YamlMetaType;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLPsiElement;
import org.jetbrains.yaml.psi.impl.YAMLPlainTextImpl;

public class ConcordYamlDelegateFactory {

    private static final Key<PsiNamedElement> DELEGATE = new Key<>("CONCORD_YAML_DELEGATE");

    private ConcordYamlDelegateFactory() {
    }

    public static @Nullable PsiNamedElement createDelegate(YAMLPsiElement psiElement) {
        PsiNamedElement omtYamlDelegate = DELEGATE.get(psiElement);
        if (omtYamlDelegate != null) {
            return omtYamlDelegate;
        }

        // create delegate by meta-type information:
        PsiNamedElement delegate = null;
        if (psiElement instanceof YAMLKeyValue) {
            delegate = createKeyValueDelegate((YAMLKeyValue) psiElement);
        } else if (psiElement instanceof YAMLPlainTextImpl) {
            delegate = createPlainTextDelegate((YAMLPlainTextImpl) psiElement);
        }
        if (delegate == null) {
            delegate = psiElement != null ? new NotADelegate(psiElement.getNode()) : null;
        }

        DELEGATE.set(psiElement, delegate);
        return delegate;
    }

    private static PsiNamedElement createKeyValueDelegate(YAMLKeyValue keyValue) {
        ConcordMetaTypeProvider instance = ConcordMetaTypeProvider.getInstance(keyValue.getProject());
        YamlMetaType valueMetaType = instance.getResolvedKeyValueMetaTypeMeta(keyValue);
//        if (valueMetaType instanceof OMTModelItemMetaType) {
//            return new OMTYamlModelItemDelegate(keyValue);
//        } else if (valueMetaType instanceof OMTIriMetaType) {
//            return new OMTYamlPrefixIriDelegate(keyValue);
//        } else if (valueMetaType instanceof OMTImportMemberMetaType) {
//            return new OMTYamlImportPathDelegate(keyValue);
//        } else if (valueMetaType instanceof OMTDeclaredModuleMetaType) {
//            return new OMTYamlDeclaredModuleDelegate(keyValue);
//        } else if (valueMetaType instanceof OMTDeclaredInterfaceMetaType) {
//            return new OMTYamlDeclaredInterfaceDelegate(keyValue);
//        }
        return null;
    }

    private static PsiNamedElement createPlainTextDelegate(YAMLPlainTextImpl yamlPlainText) {
        ConcordMetaTypeProvider instance = ConcordMetaTypeProvider.getInstance(yamlPlainText.getProject());
        YamlMetaType metaType = instance.getResolvedMetaType(yamlPlainText);
//        if (metaType instanceof OMTParamMetaType) {
//            return new OMTYamlParameterDelegate(yamlPlainText);
//        } else if (metaType instanceof OMTBindingParameterMetaType) {
//            return new OMTYamlBindingParameterDelegate(yamlPlainText);
//        } else if (metaType instanceof OMTBaseParameterMetaType) {
//            return new OMTYamlBaseParameterDelegate(yamlPlainText);
//        } else if (metaType instanceof OMTNamedVariableMetaType) {
//            return new OMTYamlVariableDelegate(yamlPlainText);
//        } else if (metaType instanceof OMTImportMemberMetaType) {
//            return new OMTYamlImportMemberDelegate(yamlPlainText);
//        } else if (metaType instanceof OMTImportedMemberRefMetaType) {
//            return new OMTYamlImportedMemberRefDelegate(yamlPlainText);
//        } else if (metaType instanceof OMTPayloadQueryReferenceMetaType) {
//            return new OMTYamlPayloadQueryReferenceDelegate(yamlPlainText);
//        } else if (metaType instanceof OMTOntologyPrefixMetaType) {
//            return new OMTYamlOntologyPrefixDelegate(yamlPlainText);
//        } else if (metaType instanceof OMTParamTypeMetaType) {
//            return new OMTYamlParamTypeDelegate(yamlPlainText);
//        } else if (metaType instanceof OMTFileReferenceMetaType) {
//            return new OMTYamlFileReferenceDelegate(yamlPlainText);
//        } else if (metaType instanceof OMTGraphShapeHandlerMemberMetaType) {
//            return new OMTYamlGraphShapeHandlerMemberDelegate(yamlPlainText);
//        }
        return null;
    }

    private static class NotADelegate extends ASTWrapperPsiElement implements PsiNamedElement {

        public NotADelegate(@NotNull ASTNode node) {
            super(node);
        }

        @Override
        public PsiElement setName(@NlsSafe @NotNull String name) throws IncorrectOperationException {
            return null;
        }
    }
}
