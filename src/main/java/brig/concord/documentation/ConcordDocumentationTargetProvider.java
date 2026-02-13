package brig.concord.documentation;

import brig.concord.completion.provider.FlowCallParamsProvider;
import brig.concord.meta.ConcordMetaTypeProvider;
import brig.concord.meta.model.IdentityMetaType;
import brig.concord.meta.model.TaskStepMetaType;
import brig.concord.meta.model.call.CallMetaType;
import brig.concord.psi.ConcordFile;
import brig.concord.schema.TaskSchemaRegistry;
import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.openapi.project.DumbService;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.DocumentationTargetProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ConcordDocumentationTargetProvider implements DocumentationTargetProvider {

    @Override
    public @NotNull List<? extends DocumentationTarget> documentationTargets(@NotNull PsiFile file, int offset) {
        if (!(file instanceof ConcordFile)) {
            return List.of();
        }

        var element = file.findElementAt(offset);
        if (element == null) {
            return List.of();
        }

        var elementType = PsiUtilCore.getElementType(element);

        // Check for flow name values (TEXT, SCALAR_STRING, SCALAR_DSTRING)
        if (elementType == YAMLTokenTypes.TEXT
                || elementType == YAMLTokenTypes.SCALAR_STRING
                || elementType == YAMLTokenTypes.SCALAR_DSTRING) {
            var flowDocTarget = resolveFlowCallDocumentation(file, element.getParent());
            if (flowDocTarget != null) {
                return List.of(flowDocTarget);
            }

            var taskDocTarget = resolveTaskDocumentation(file, element.getParent());
            if (taskDocTarget != null) {
                return List.of(taskDocTarget);
            }
        }

        // Navigate from scalar key token to the YAMLKeyValue parent
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

    private static @Nullable FlowDocumentationTarget resolveFlowCallDocumentation(
            @NotNull PsiFile file, @Nullable PsiElement parent) {
        if (!(parent instanceof YAMLScalar scalar)) {
            return null;
        }

        if (DumbService.isDumb(file.getProject())) {
            return null;
        }

        var metaTypeProvider = ConcordMetaTypeProvider.getInstance(file.getProject());
        var metaType = metaTypeProvider.getResolvedMetaType(scalar);
        if (!(metaType instanceof CallMetaType)) {
            return null;
        }

        var flowDoc = FlowCallParamsProvider.findFlowDocumentation(scalar);
        if (flowDoc == null) {
            return null;
        }

        var callKv = FlowCallParamsProvider.findCallKv(scalar);
        if (callKv == null) {
            return null;
        }

        return new FlowDocumentationTarget(callKv, flowDoc);
    }

    private static @Nullable TaskDocumentationTarget resolveTaskDocumentation(
            @NotNull PsiFile file, @Nullable PsiElement parent) {
        if (!(parent instanceof YAMLScalar scalar)) {
            return null;
        }

        var metaTypeProvider = ConcordMetaTypeProvider.getInstance(file.getProject());
        var metaType = metaTypeProvider.getResolvedMetaType(scalar);
        if (!(metaType instanceof TaskStepMetaType.TaskNameMetaType)) {
            return null;
        }

        var taskName = scalar.getTextValue();
        if (taskName == null || taskName.isBlank()) {
            return null;
        }

        var schema = TaskSchemaRegistry.getInstance(file.getProject()).getSchema(taskName);
        if (schema == null) {
            return null;
        }

        var taskKv = scalar.getParent();
        if (!(taskKv instanceof YAMLKeyValue kv)) {
            return null;
        }

        return new TaskDocumentationTarget(kv, schema);
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
