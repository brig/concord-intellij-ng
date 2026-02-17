package brig.concord.psi;

import brig.concord.psi.VariablesProvider.Variable;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.openapi.components.Service;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Service(Service.Level.PROJECT)
public final class ElPropertyProviderImpl implements ElPropertyProvider {

    private static final int MAX_FOLLOW_DEPTH = 3;

    private static final List<ElPropertySubProvider> SUB_PROVIDERS = List.of(
            new YamlMappingPropertySubProvider(),
            new BuiltInPropertySubProvider(),
            new TaskOutPropertySubProvider()
    );

    @Override
    public @NotNull List<PropertyItem> getProperties(@NotNull List<String> accessChain,
                                                     @NotNull PsiElement yamlContext) {
        return getProperties(accessChain, yamlContext, 0);
    }

    private @NotNull List<PropertyItem> getProperties(@NotNull List<String> accessChain,
                                                      @NotNull PsiElement yamlContext,
                                                      int depth) {
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

        if (result.isEmpty() && depth < MAX_FOLLOW_DEPTH) {
            var refChain = extractElChain(variable);
            if (refChain != null) {
                refChain.addAll(intermediateSegments);
                return getProperties(refChain, yamlContext, depth + 1);
            }
        }

        return result;
    }

    @Override
    public @Nullable PsiElement resolveProperty(@NotNull List<String> accessChain,
                                                @NotNull String propertyName,
                                                @NotNull PsiElement yamlContext) {
        return resolveProperty(accessChain, propertyName, yamlContext, 0);
    }

    private @Nullable PsiElement resolveProperty(@NotNull List<String> accessChain,
                                                 @NotNull String propertyName,
                                                 @NotNull PsiElement yamlContext,
                                                 int depth) {
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

        if (depth < MAX_FOLLOW_DEPTH) {
            var refChain = extractElChain(variable);
            if (refChain != null) {
                refChain.addAll(intermediateSegments);
                return resolveProperty(refChain, propertyName, yamlContext, depth + 1);
            }
        }

        return null;
    }

    private static @Nullable ArrayList<String> extractElChain(@NotNull Variable variable) {
        if (!(variable.declaration() instanceof YAMLKeyValue kv)) {
            return null;
        }
        if (!(kv.getValue() instanceof YAMLScalar scalar)) {
            return null;
        }
        var chain = ElAccessChainExtractor.extractFullChain(scalar);
        if (chain == null) {
            return null;
        }
        return new ArrayList<>(chain);
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
