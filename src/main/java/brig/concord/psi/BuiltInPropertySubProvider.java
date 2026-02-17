package brig.concord.psi;

import brig.concord.psi.ElPropertyProvider.PropertyItem;
import brig.concord.psi.VariablesProvider.Variable;
import brig.concord.psi.VariablesProvider.VariableSource;
import brig.concord.schema.BuiltInVarSchemaRegistry;
import brig.concord.schema.SchemaType;
import brig.concord.schema.TaskSchemaSection;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class BuiltInPropertySubProvider implements ElPropertySubProvider {

    @Override
    public boolean supports(@NotNull Variable variable) {
        return variable.source() == VariableSource.BUILT_IN;
    }

    @Override
    public @NotNull List<PropertyItem> getProperties(@NotNull Variable variable,
                                                     @NotNull List<String> intermediateSegments,
                                                     @NotNull PsiElement yamlContext) {
        var section = resolveSection(variable.name(), intermediateSegments);
        if (section == null) {
            return List.of();
        }

        var result = new ArrayList<PropertyItem>();
        for (var prop : section.properties().values()) {
            result.add(new PropertyItem(prop.name(), "built-in", prop.description(), null));
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

    private static @Nullable TaskSchemaSection resolveSection(@NotNull String varName,
                                                              @NotNull List<String> intermediateSegments) {
        var section = BuiltInVarSchemaRegistry.getSchema(varName);
        if (section == null) {
            return null;
        }

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
}
