// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi.impl.delegate;

import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.LoopArrayItemMetaType;
import brig.concord.meta.model.OutVarContainerMetaType;
import brig.concord.meta.model.call.CallInParamMetaType;
import brig.concord.meta.model.call.CallInParamsMetaType;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.yaml.psi.impl.YAMLQuotedTextImpl;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLPsiElement;
import brig.concord.yaml.psi.impl.YAMLPlainTextImpl;

public class ConcordYamlDelegateFactory {

    private static final Key<PsiNamedElement> DELEGATE = new Key<>("CONCORD_YAML_DELEGATE");

    private ConcordYamlDelegateFactory() {
    }

    public static @Nullable PsiNamedElement createDelegate(YAMLPsiElement psiElement) {
        PsiNamedElement yamlDelegate = DELEGATE.get(psiElement);
        if (yamlDelegate != null) {
            return yamlDelegate;
        }

        PsiNamedElement delegate = null;
        if (psiElement instanceof YAMLKeyValue) {
            delegate = createKeyValueDelegate((YAMLKeyValue) psiElement);
        } else if (psiElement instanceof YAMLPlainTextImpl) {
            delegate = createPlainTextDelegate((YAMLPlainTextImpl) psiElement);
        } else if (psiElement instanceof YAMLQuotedTextImpl) {
            delegate = createQuoteTextDelegate((YAMLQuotedTextImpl) psiElement);
        }

        if (delegate == null) {
            delegate = psiElement != null ? new NotADelegate(psiElement.getNode()) : null;
        }

        DELEGATE.set(psiElement, delegate);
        return delegate;
    }

    private static PsiNamedElement createKeyValueDelegate(YAMLKeyValue keyValue) {
        ConcordMetaTypeProvider instance = ConcordMetaTypeProvider.getInstance(keyValue.getProject());
        YamlMetaType valueMetaType = instance.getResolvedMetaType(keyValue);
        if (valueMetaType instanceof CallInParamsMetaType) {
            return new YamlFlowCallInParamDelegate(keyValue);
        }

        // Check if this key-value is inside a call in-params mapping
        var parentMapping = keyValue.getParentMapping();
        if (parentMapping != null) {
            YamlMetaType parentMetaType = instance.getResolvedMetaType(parentMapping);
            if (parentMetaType instanceof CallInParamMetaType) {
                return new YamlFlowCallInParamDelegate(keyValue);
            }
        }

        return null;
    }

    private static PsiNamedElement createPlainTextDelegate(YAMLPlainTextImpl yamlPlainText) {
        ConcordMetaTypeProvider instance = ConcordMetaTypeProvider.getInstance(yamlPlainText.getProject());
        YamlMetaType metaType = instance.getResolvedMetaType(yamlPlainText);
        if (metaType instanceof CallMetaType || metaType instanceof LoopArrayItemMetaType) {
            return new YamlPlainTextFlowCallDelegate(yamlPlainText);
        }
        if (metaType instanceof OutVarContainerMetaType) {
            return new YamlOutVarDelegate(yamlPlainText.getNode(), yamlPlainText);
        }
        return null;
    }

    private static PsiNamedElement createQuoteTextDelegate(YAMLQuotedTextImpl quotedText) {
        ConcordMetaTypeProvider instance = ConcordMetaTypeProvider.getInstance(quotedText.getProject());
        YamlMetaType metaType = instance.getResolvedMetaType(quotedText);
        if (metaType instanceof CallMetaType || metaType instanceof LoopArrayItemMetaType) {
            return new YamlQuoteTextFlowCallDelegate(quotedText);
        }
        if (metaType instanceof OutVarContainerMetaType) {
            return new YamlOutVarDelegate(quotedText.getNode(), quotedText);
        }
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
