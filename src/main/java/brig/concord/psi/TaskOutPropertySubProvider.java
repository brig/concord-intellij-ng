package brig.concord.psi;

import brig.concord.psi.ElPropertyProvider.PropertyItem;
import brig.concord.psi.VariablesProvider.Variable;
import brig.concord.psi.VariablesProvider.VariableSource;
import brig.concord.schema.SchemaType;
import brig.concord.schema.TaskSchemaRegistry;
import brig.concord.schema.TaskSchemaSection;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides properties for {@code STEP_OUT} variables based on the task's output schema.
 * <p>
 * For {@code - task: concord / out: result}, resolves {@code result.<caret>} using
 * the "out" section of {@code concord.schema.json}.
 */
final class TaskOutPropertySubProvider implements ElPropertySubProvider {

    @Override
    public boolean supports(@NotNull Variable variable) {
        return variable.source() == VariableSource.STEP_OUT;
    }

    @Override
    public @NotNull List<PropertyItem> getProperties(@NotNull Variable variable,
                                                     @NotNull List<String> intermediateSegments,
                                                     @NotNull PsiElement yamlContext) {
        var section = resolveSection(variable, intermediateSegments);
        if (section == null) {
            return List.of();
        }

        var result = new ArrayList<PropertyItem>();
        for (var prop : section.properties().values()) {
            result.add(new PropertyItem(prop.name(), "task out", prop.description(), null));
        }
        return result;
    }

    @Override
    public @Nullable PsiElement resolveProperty(@NotNull Variable variable,
                                                @NotNull List<String> intermediateSegments,
                                                @NotNull String propertyName,
                                                @NotNull PsiElement yamlContext) {
        return null;
    }

    private static @Nullable TaskSchemaSection resolveSection(@NotNull Variable variable,
                                                              @NotNull List<String> intermediateSegments) {
        var outSection = findOutSection(variable);
        if (outSection == null) {
            return null;
        }

        var section = outSection;
        for (var segment : intermediateSegments) {
            var prop = section.properties().get(segment);
            if (prop == null) {
                return null;
            }
            if (prop.schemaType() instanceof SchemaType.Object obj) {
                section = obj.section();
            } else {
                return null;
            }
        }

        return section;
    }

    private static @Nullable TaskSchemaSection findOutSection(@NotNull Variable variable) {
        var declaration = variable.declaration();
        if (declaration == null) {
            return null;
        }

        var stepMapping = findStepMapping(declaration);
        if (stepMapping == null) {
            return null;
        }

        var taskName = extractTaskName(stepMapping);
        if (taskName == null) {
            return null;
        }

        var schema = TaskSchemaRegistry.getInstance(declaration.getProject()).getSchema(taskName);
        if (schema == null) {
            return null;
        }

        var outSection = schema.getOutSection();
        if (outSection.properties().isEmpty()) {
            return null;
        }

        return outSection;
    }

    private static @Nullable YAMLMapping findStepMapping(@NotNull PsiElement declaration) {
        // For scalar out (out: result): scalar -> YAMLKeyValue(out) -> YAMLMapping(step)
        // For mapping out (out: {k: v}): YAMLKeyValue(k) -> YAMLMapping(out value) -> YAMLKeyValue(out) -> YAMLMapping(step)
        var parent = declaration.getParent();
        if (parent instanceof YAMLKeyValue outKv && "out".equals(outKv.getKeyText())) {
            return outKv.getParentMapping();
        }

        // scalar in a sequence: scalar -> YAMLSequenceItem -> YAMLSequence -> YAMLKeyValue(out) -> YAMLMapping
        if (parent != null) {
            var grandParent = parent.getParent();
            if (grandParent != null) {
                var outKvCandidate = grandParent.getParent();
                if (outKvCandidate instanceof YAMLKeyValue outKv && "out".equals(outKv.getKeyText())) {
                    return outKv.getParentMapping();
                }
            }
        }
        // For mapping out, declaration is YAMLKeyValue inside the out mapping
        if (declaration instanceof YAMLKeyValue kv) {
            var mapping = kv.getParentMapping();
            if (mapping != null) {
                var mappingParent = mapping.getParent();
                if (mappingParent instanceof YAMLKeyValue outKv && "out".equals(outKv.getKeyText())) {
                    return outKv.getParentMapping();
                }
            }
        }
        return null;
    }

    private static @Nullable String extractTaskName(@NotNull YAMLMapping stepMapping) {
        var taskKv = stepMapping.getKeyValueByKey("task");
        if (taskKv == null) {
            return null;
        }
        return taskKv.getValue() instanceof YAMLScalar scalar ? scalar.getTextValue() : null;
    }
}
