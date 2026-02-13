package brig.concord.documentation;

import brig.concord.meta.model.TaskStepMetaType.TaskNameLookup;
import brig.concord.psi.ConcordFile;
import brig.concord.schema.TaskSchemaMetaType;
import brig.concord.schema.TaskSchemaRegistry;
import brig.concord.yaml.meta.model.TypeFieldPair;
import brig.concord.yaml.meta.model.YamlComposedTypeBase;
import brig.concord.yaml.meta.model.YamlMetaType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.LookupElementDocumentationTargetProvider;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ConcordLookupDocumentationProvider implements LookupElementDocumentationTargetProvider {

    @Override
    public @Nullable DocumentationTarget documentationTarget(@NotNull PsiFile psiFile,
                                                              @NotNull LookupElement element,
                                                              int offset) {
        if (!(psiFile instanceof ConcordFile)) {
            return null;
        }

        var obj = element.getObject();

        if (obj instanceof TypeFieldPair pair) {
            return resolveKeyDocumentation(pair);
        }

        if (obj instanceof TaskNameLookup taskLookup) {
            return resolveTaskNameDocumentation(psiFile.getProject(), taskLookup);
        }

        return null;
    }

    private static @Nullable DocumentationTarget resolveKeyDocumentation(@NotNull TypeFieldPair pair) {
        var field = pair.getField();
        var metaType = field.getOriginalType();

        // Meta type has its own description
        if (metaType.getDescription() != null) {
            return new ConcordDocumentationTarget(field.getName(), metaType, metaType.getTypeName());
        }

        // Check if owner (or its sub-type) is a TaskSchemaMetaType with a property description
        var taskSchema = findTaskSchemaMetaType(pair.getMetaType());
        if (taskSchema != null) {
            var desc = taskSchema.getPropertyDescription(field.getName());
            if (desc != null) {
                Documented documented = Documented.ofDescription(desc);
                return new ConcordDocumentationTarget(field.getName(), documented, metaType.getTypeName());
            }
        }

        return null;
    }

    private static @Nullable DocumentationTarget resolveTaskNameDocumentation(@NotNull Project project,
                                                                               @NotNull TaskNameLookup lookup) {
        var schema = TaskSchemaRegistry.getInstance(project).getSchema(lookup.name());
        if (schema == null) {
            return null;
        }

        var section = schema.getBaseInSection();
        if (section.properties().isEmpty()) {
            return null;
        }

        var metaType = new TaskSchemaMetaType(section, schema.getDiscriminatorKeys());
        return new ConcordDocumentationTarget(lookup.name(), metaType, "task");
    }

    private static @Nullable TaskSchemaMetaType findTaskSchemaMetaType(@NotNull YamlMetaType type) {
        if (type instanceof TaskSchemaMetaType tsm) {
            return tsm;
        }
        if (type instanceof YamlComposedTypeBase composed) {
            for (var sub : composed.getSubTypes()) {
                var result = findTaskSchemaMetaType(sub);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
