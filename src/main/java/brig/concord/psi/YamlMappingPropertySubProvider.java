package brig.concord.psi;

import brig.concord.psi.ElPropertyProvider.PropertyItem;
import brig.concord.psi.VariablesProvider.Variable;
import brig.concord.psi.VariablesProvider.VariableSource;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLMapping;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class YamlMappingPropertySubProvider implements ElPropertySubProvider {

    @Override
    public boolean supports(@NotNull Variable variable) {
        return variable.source() == VariableSource.SET_STEP
                || variable.source() == VariableSource.ARGUMENT;
    }

    @Override
    public @NotNull List<PropertyItem> getProperties(@NotNull Variable variable,
                                                     @NotNull List<String> intermediateSegments,
                                                     @NotNull PsiElement yamlContext) {
        var mapping = resolveMapping(variable, intermediateSegments);
        if (mapping == null) {
            return List.of();
        }

        var sourceLabel = sourceLabel(variable.source());
        var result = new ArrayList<PropertyItem>();
        for (var kv : mapping.getKeyValues()) {
            var name = kv.getKeyText().trim();
            if (!name.isEmpty()) {
                result.add(new PropertyItem(name, sourceLabel, null, kv));
            }
        }
        return result;
    }

    @Override
    public @Nullable PsiElement resolveProperty(@NotNull Variable variable,
                                                @NotNull List<String> intermediateSegments,
                                                @NotNull String propertyName,
                                                @NotNull PsiElement yamlContext) {
        var mapping = resolveMapping(variable, intermediateSegments);
        if (mapping == null) {
            return null;
        }
        return mapping.getKeyValueByKey(propertyName);
    }

    private static @Nullable YAMLMapping resolveMapping(@NotNull Variable variable,
                                                        @NotNull List<String> intermediateSegments) {
        var declaration = variable.declaration();
        if (!(declaration instanceof YAMLKeyValue kv)) {
            return null;
        }

        var value = kv.getValue();
        if (!(value instanceof YAMLMapping mapping)) {
            return null;
        }

        for (var segment : intermediateSegments) {
            var child = mapping.getKeyValueByKey(segment);
            if (child == null) {
                return null;
            }
            var childValue = child.getValue();
            if (!(childValue instanceof YAMLMapping childMapping)) {
                return null;
            }
            mapping = childMapping;
        }

        return mapping;
    }

    private static @NotNull String sourceLabel(@NotNull VariableSource source) {
        return switch (source) {
            case SET_STEP -> "set";
            case ARGUMENT -> "argument";
            default -> source.name();
        };
    }
}
