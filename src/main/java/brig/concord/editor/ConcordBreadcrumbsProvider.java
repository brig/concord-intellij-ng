package brig.concord.editor;

import brig.concord.ConcordLanguage;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.*;
import brig.concord.psi.ProcessDefinition;
import brig.concord.yaml.psi.*;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiTreeUtil.getChildOfType;

public class ConcordBreadcrumbsProvider implements BreadcrumbsProvider {

    private static final int MAX_TEXT_LENGTH = 30;

    private static final Language[] LANGUAGES = {ConcordLanguage.INSTANCE};

    @Override
    public Language[] getLanguages() {
        return LANGUAGES;
    }

    @Override
    public boolean acceptElement(@NotNull PsiElement element) {
        if (element instanceof YAMLKeyValue kv) {
            return ProcessDefinition.isFlowDefinition(kv);
        } else if (element instanceof YAMLSequenceItem) {
            var metaProvider = ConcordMetaTypeProvider.getInstance(element.getProject());
            var metaType = metaProvider.getResolvedMetaType(element);
            return metaType instanceof StepElementMetaType;
        }
        return false;
    }

    @Override
    public @NotNull String getElementInfo(@NotNull PsiElement element) {
        String result = null;
        if (element instanceof YAMLKeyValue kv) {
            result = kv.getKeyText();
        } else if (element instanceof YAMLSequenceItem item) {
            result = getSequenceItemInfoText(item);
        }
        return truncate(result);
    }

    @Nullable
    private static String getSequenceItemInfoText(@NotNull YAMLSequenceItem item) {
        var mapping = getChildOfType(item, YAMLMapping.class);
        if (mapping == null) {
            var scalar = getChildOfType(item, YAMLScalar.class);
            if (scalar != null) {
                return scalar.getTextValue();
            }
            return null;
        }

        // 1. Prefer explicit 'name'
        var nameKv = mapping.getKeyValueByKey("name");
        if (nameKv != null) {
            var value = getValueText(nameKv);
            if (value != null) {
                return value;
            }
        }

        // 2. Identify step type using MetaType
        var metaProvider = ConcordMetaTypeProvider.getInstance(item.getProject());
        var metaType = metaProvider.getResolvedMetaType(mapping);

        if (metaType instanceof IdentityMetaType identityMeta) {
            var identityKey = identityMeta.getIdentity();

            // Try to get the value of the identity key (e.g., call: myFlow -> myFlow)
            var identityKv = mapping.getKeyValueByKey(identityKey);
            if (identityKv != null) {
                var value = getValueText(identityKv);
                if (value != null) {
                    return identityKey + ": " + value;
                }
            }
            return identityKey;
        }

        return null;
    }

    @Nullable
    private static String getValueText(@NotNull YAMLKeyValue kv) {
        var value = kv.getValue();
        if (value instanceof YAMLScalar scalar) {
            return scalar.getTextValue();
        }
        return null;
    }

    @NotNull
    private static String truncate(@Nullable String text) {
        if (text == null) {
            return "";
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            return text.substring(0, MAX_TEXT_LENGTH) + "...";
        }
        return text;
    }
}
