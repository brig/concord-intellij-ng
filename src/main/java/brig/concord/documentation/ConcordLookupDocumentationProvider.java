// SPDX-License-Identifier: Apache-2.0
package brig.concord.documentation;

import brig.concord.completion.ElCompletionContributor.FunctionLookup;
import brig.concord.completion.ElCompletionContributor.PropertyLookup;
import brig.concord.completion.ElCompletionContributor.TaskMethodLookup;
import brig.concord.completion.ElCompletionContributor.TaskNameElLookup;
import brig.concord.completion.ElCompletionContributor.VariableLookup;
import brig.concord.meta.model.TaskStepMetaType.TaskNameLookup;
import brig.concord.meta.model.call.CallOutValueMetaType.OutParameterLookup;
import brig.concord.psi.ConcordFile;
import brig.concord.schema.*;
import brig.concord.yaml.meta.model.TypeFieldPair;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.project.Project;
import com.intellij.platform.backend.documentation.DocumentationTarget;
import com.intellij.platform.backend.documentation.LookupElementDocumentationTargetProvider;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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

        if (obj instanceof OutParameterLookup outParam) {
            return resolveOutParameterDocumentation(outParam);
        }

        if (obj instanceof VariableLookup varLookup) {
            return resolveVariableDocumentation(varLookup);
        }

        if (obj instanceof PropertyLookup propLookup) {
            return resolvePropertyDocumentation(propLookup);
        }

        if (obj instanceof FunctionLookup funcLookup) {
            return resolveFunctionDocumentation(funcLookup);
        }

        if (obj instanceof TaskNameElLookup taskElLookup) {
            return resolveTaskNameElDocumentation(psiFile.getProject(), taskElLookup);
        }

        if (obj instanceof TaskMethodLookup methodLookup) {
            return resolveTaskMethodDocumentation(methodLookup);
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

    private static @NotNull DocumentationTarget resolveOutParameterDocumentation(@NotNull OutParameterLookup lookup) {
        var description = lookup.description();
        var mandatory = lookup.mandatory() ? "mandatory" : "optional";
        var fullDescription = description != null && !description.isEmpty()
                ? mandatory + " — " + description
                : mandatory;

        return new ConcordDocumentationTarget(lookup.name(), Documented.ofDescription(fullDescription), lookup.type());
    }

    private static @NotNull DocumentationTarget resolveVariableDocumentation(@NotNull VariableLookup lookup) {
        var schema = lookup.schema();
        var typeText = schema != null ? SchemaType.displayName(schema.schemaType()) : "n/a";

        var documented = new Documented() {

            @Override
            public @Nullable String getDescription() {
                if (schema != null) {
                    return schema.description();
                }
                return null;
            }

            @Override
            public @NotNull List<DocumentedField> getDocumentationFields() {
                if (schema == null) {
                    return List.of();
                }
                return objectFields(schema.schemaType());
            }

            @Override
            public @NotNull String getDocumentationFooter() {
                return "<p><b>source</b>: " + lookup.source().description() + "</p>";
            }
        };

        return new ConcordDocumentationTarget(lookup.name(), documented, typeText);
    }

    private static @NotNull DocumentationTarget resolvePropertyDocumentation(@NotNull PropertyLookup lookup) {
        var schema = lookup.schema();
        var typeText = SchemaType.displayName(schema.schemaType());

        var documented = new Documented() {

            @Override
            public @Nullable String getDescription() {
                return schema.description();
            }

            @Override
            public @NotNull List<DocumentedField> getDocumentationFields() {
                return objectFields(schema.schemaType());
            }
        };

        return new ConcordDocumentationTarget(lookup.name(), documented, typeText);
    }

    private static @NotNull DocumentationTarget resolveFunctionDocumentation(@NotNull FunctionLookup lookup) {
        var function = lookup.function();
        var returnTypeText = SchemaType.displayName(function.returnType());

        var paramFields = function.params().stream()
                .map(p -> new Documented.DocumentedField(
                        p.name(), SchemaType.displayName(p.schemaType()), p.required(), p.description(), List.of()))
                .toList();

        var documented = new Documented() {

            @Override
            public @Nullable String getDescription() {
                return function.description();
            }

            @Override
            public @NotNull List<DocumentedSection> getDocumentationSections() {
                if (paramFields.isEmpty()) {
                    return List.of();
                }
                return List.of(new DocumentedSection("Parameters:", paramFields));
            }

            @Override
            public @NotNull String getDocumentationFooter() {
                return "<p><b>Returns</b>: <code>" + returnTypeText + "</code></p>";
            }
        };

        return new ConcordDocumentationTarget(function.name() + function.signature(), documented, "function");
    }

    private static @NotNull DocumentationTarget resolveTaskNameElDocumentation(@NotNull Project project,
                                                                                @NotNull TaskNameElLookup lookup) {
        var schema = TaskSchemaRegistry.getInstance(project).getSchema(lookup.name());
        if (schema != null) {
            return new TaskDocumentationTarget(schema);
        }

        return new ConcordDocumentationTarget(lookup.name(), Documented.ofDescription("Concord task"), "task");
    }

    private static @NotNull DocumentationTarget resolveTaskMethodDocumentation(@NotNull TaskMethodLookup lookup) {
        var method = lookup.method();
        var returnTypeText = SchemaType.displayName(method.returnType());

        var paramTypes = method.parameterTypes();
        var paramFields = new ArrayList<Documented.DocumentedField>();
        for (int i = 0; i < paramTypes.size(); i++) {
            paramFields.add(new Documented.DocumentedField(
                    "arg" + i, SchemaType.displayName(paramTypes.get(i)), false, null, List.of()));
        }

        var documented = new Documented() {

            @Override
            public @Nullable String getDescription() {
                return "Method on task <code>" + lookup.taskName() + "</code>";
            }

            @Override
            public @NotNull List<DocumentedSection> getDocumentationSections() {
                if (paramFields.isEmpty()) {
                    return List.of();
                }
                return List.of(new DocumentedSection("Parameters:", paramFields));
            }

            @Override
            public @NotNull String getDocumentationFooter() {
                return "<p><b>Returns</b>: <code>" + returnTypeText + "</code></p>";
            }
        };

        return new ConcordDocumentationTarget(method.name() + method.signature(), documented, returnTypeText);
    }

    private static @NotNull List<Documented.DocumentedField> objectFields(@NotNull SchemaType schemaType) {
        if (!(schemaType instanceof SchemaType.Object(ObjectSchema section))) {
            return List.of();
        }

        var fields = new ArrayList<Documented.DocumentedField>();
        for (var prop : section.properties().values()) {
            fields.add(new Documented.DocumentedField(
                    prop.name(),
                    SchemaType.displayName(prop.schemaType()),
                    prop.required(),
                    prop.description(),
                    List.of()
            ));
        }
        return fields;
    }

}
