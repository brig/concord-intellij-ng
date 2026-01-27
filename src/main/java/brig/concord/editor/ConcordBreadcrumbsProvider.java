package brig.concord.editor;

import brig.concord.ConcordLanguage;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.*;
import brig.concord.meta.model.call.CallInParamsMetaType;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.meta.model.call.CallOutParamsMetaType;
import brig.concord.psi.ProcessDefinition;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLSequenceItem;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.ui.breadcrumbs.BreadcrumbsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.psi.util.PsiTreeUtil.getChildOfType;

public class ConcordBreadcrumbsProvider implements BreadcrumbsProvider {

    private static final int MAX_TEXT_LENGTH = 30;

    @Override
    public Language[] getLanguages() {
        return new Language[]{ConcordLanguage.INSTANCE};
    }

        @Override
        public boolean acceptElement(@NotNull PsiElement element) {
            if (element instanceof YAMLKeyValue kv) {
                return ProcessDefinition.isFlowDefinition(kv);
            }
            return element instanceof YAMLSequenceItem;
        }
    
        @Override
        public @NotNull String getElementInfo(@NotNull PsiElement element) {
            if (element instanceof YAMLKeyValue kv) {
                return kv.getKeyText();
            } else if (element instanceof YAMLSequenceItem item) {
                return getSequenceItemInfo(item);
            }
            return "";
        }
    
        private String getSequenceItemInfo(YAMLSequenceItem item) {
            // Find if this sequence item is a step mapping
            var mapping = getChildOfType(item, YAMLMapping.class);
            if (mapping == null) {
                return "Item";
            }
    
            // 1. Prefer explicit 'name'
            var nameKv = mapping.getKeyValueByKey("name");
            if (nameKv != null && nameKv.getValueText() != null) {
                return truncate(nameKv.getValueText());
            }
    
            // 2. Identify step type using MetaType
            var metaProvider = ConcordMetaTypeProvider.getInstance(item.getProject());
            var metaType = metaProvider.getResolvedMetaType(mapping);
    
            if (metaType instanceof IdentityMetaType identityMeta) {
                var displayName = identityMeta.getDisplayName();
                var identityKey = identityMeta.getIdentity();
    
                // Try to get the value of the identity key (e.g., call: myFlow -> myFlow)
                var identityKv = mapping.getKeyValueByKey(identityKey);
                if (identityKv != null) {
                    var value = getValueText(identityKv);
                    if (!value.isEmpty() && !value.equals("?")) {
                        return displayName + ": " + value;
                    }
                }
                return displayName;
            }
    
            return "Step";
        }
    private boolean isRootKey(YAMLKeyValue kv) {
        var parentMapping = kv.getParentMapping();
        return parentMapping != null && parentMapping.getParent() instanceof brig.concord.yaml.psi.YAMLDocument;
    }

    private String getValueText(@Nullable YAMLKeyValue kv) {
        if (kv == null) return "?";
        String value = kv.getValueText();
        return value != null ? truncate(value) : "?";
    }

    private String truncate(String text) {
        if (text == null) return "";
        if (text.length() > MAX_TEXT_LENGTH) {
            return text.substring(0, MAX_TEXT_LENGTH) + "...";
        }
        return text;
    }
}
