package brig.concord.documentation;

import brig.concord.meta.model.TaskStepMetaType.TaskNameLookup;
import brig.concord.psi.ConcordFile;
import brig.concord.schema.TaskSchemaRegistry;
import brig.concord.yaml.meta.model.TypeFieldPair;
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

        if (metaType.getDescription() != null) {
            return new ConcordDocumentationTarget(field.getName(), metaType, metaType.getTypeName());
        }

        return null;
    }

    private static @Nullable DocumentationTarget resolveTaskNameDocumentation(@NotNull Project project,
                                                                               @NotNull TaskNameLookup lookup) {
        var schema = TaskSchemaRegistry.getInstance(project).getSchema(lookup.name());
        if (schema == null) {
            return null;
        }

        return new TaskDocumentationTarget(schema);
    }

}
