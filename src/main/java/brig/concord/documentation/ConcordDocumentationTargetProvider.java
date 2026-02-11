package brig.concord.documentation;

import brig.concord.meta.ConcordMetaType;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.psi.ConcordFile;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
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
                return List.of(new ConcordDocumentationTarget(kv, metaType, metaType.getDisplayName()));
            }
        }
        return List.of();
    }

    private static @Nullable YamlMetaType resolveDocumentationType(
            ConcordMetaTypeProvider provider, YAMLKeyValue kv) {
        // Try parent feature lookup first — this preserves array types with doc prefixes
        // that would be lost via Field unwrapping in the normal resolution path
        var parentMapping = kv.getParentMapping();
        if (parentMapping != null) {
            var parentMetaType = provider.getResolvedMetaType(parentMapping);
            if (parentMetaType instanceof ConcordMetaType concordParent) {
                var featureType = concordParent.getFeatureMetaType(kv.getKeyText().trim());
                if (featureType != null && featureType.getDescription() != null) {
                    return featureType;
                }
            }
        }
        // Fallback: standard value-type resolution (works for non-array leaf types)
        return provider.getResolvedKeyValueMetaTypeMeta(kv);
    }
}
