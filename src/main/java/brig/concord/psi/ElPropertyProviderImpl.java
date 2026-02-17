package brig.concord.psi;

import brig.concord.psi.VariablesProvider.Variable;
import com.intellij.openapi.components.Service;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class ElPropertyProviderImpl implements ElPropertyProvider {

    private static final List<ElPropertySubProvider> SUB_PROVIDERS = List.of(
            new YamlMappingPropertySubProvider()
    );

    @Override
    public @NotNull List<PropertyItem> getProperties(@NotNull List<String> accessChain,
                                                     @NotNull PsiElement yamlContext) {
        if (accessChain.isEmpty()) {
            return List.of();
        }

        var variableName = accessChain.getFirst();
        var intermediateSegments = accessChain.subList(1, accessChain.size());

        var variable = findVariable(variableName, yamlContext);
        if (variable == null) {
            return List.of();
        }

        var result = new ArrayList<PropertyItem>();
        for (var subProvider : SUB_PROVIDERS) {
            if (subProvider.supports(variable)) {
                result.addAll(subProvider.getProperties(variable, intermediateSegments, yamlContext));
            }
        }
        return result;
    }

    @Override
    public @Nullable PsiElement resolveProperty(@NotNull List<String> accessChain,
                                                @NotNull String propertyName,
                                                @NotNull PsiElement yamlContext) {
        if (accessChain.isEmpty()) {
            return null;
        }

        var variableName = accessChain.getFirst();
        var intermediateSegments = accessChain.subList(1, accessChain.size());

        var variable = findVariable(variableName, yamlContext);
        if (variable == null) {
            return null;
        }

        for (var subProvider : SUB_PROVIDERS) {
            if (subProvider.supports(variable)) {
                var resolved = subProvider.resolveProperty(variable, intermediateSegments, propertyName, yamlContext);
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return null;
    }

    private static @Nullable Variable findVariable(@NotNull String name, @NotNull PsiElement yamlContext) {
        var variables = VariablesProvider.getVariables(yamlContext);
        for (var variable : variables) {
            if (name.equals(variable.name())) {
                return variable;
            }
        }
        return null;
    }
}
