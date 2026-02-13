package brig.concord.documentation;

import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.IdentityMetaType;
import brig.concord.psi.ConcordFile;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import com.intellij.platform.backend.documentation.DocumentationTargetProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConcordDocumentationTargetProvider implements DocumentationTargetProvider {

    @Override
    public @NotNull List<ConcordDocumentationTarget> documentationTargets(@NotNull PsiFile file, int offset) {
        if (!(file instanceof ConcordFile)) {
            return List.of();
        }

        var element = file.findElementAt(offset);
        if (element == null) {
            return List.of();
        }

        // Navigate from scalar key token to the YAMLKeyValue parent
        var elementType = PsiUtilCore.getElementType(element);
        if (elementType == YAMLTokenTypes.SCALAR_KEY) {
            element = element.getParent();
        }

        var metaTypeProvider = ConcordMetaTypeProvider.getInstance(file.getProject());
        if (element instanceof YAMLKeyValue kv) {
            var metaType = resolveDocumentationType(metaTypeProvider, kv);
            if (metaType != null && metaType.getDescription() != null) {
                return List.of(new ConcordDocumentationTarget(kv, metaType, metaType.getTypeName()));
            }
        }
        return List.of();
    }

    private static @Nullable YamlMetaType resolveDocumentationType(
            ConcordMetaTypeProvider provider, YAMLKeyValue kv) {
        // Check if this key is the identity key of a step (or similar identity-based type)
        // Only use the identity type if it has a description; otherwise fall through
        // to the value meta type resolution for backwards compatibility
        var parent = kv.getParent();
        if (parent instanceof YAMLMapping mapping) {
            var mappingProxy = provider.getMetaTypeProxy(mapping);
            if (mappingProxy != null
                    && mappingProxy.getMetaType() instanceof IdentityMetaType identityType
                    && identityType.getIdentity().equals(kv.getKeyText())
                    && identityType.getDescription() != null) {
                return identityType;
            }
        }

        // Fallback to value meta type
        var proxy = provider.getKeyValueMetaType(kv);
        if (proxy == null) {
            return null;
        }
        return proxy.getField().getOriginalType();
    }
}
