package brig.concord.psi;

import brig.concord.el.psi.*;
import brig.concord.schema.ObjectSchema;
import brig.concord.schema.SchemaProperty;
import brig.concord.schema.SchemaType;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Resolves schema types at any point in an EL dot-access chain (e.g., {@code initiator.username}).
 * Used by both completion and reference contributors.
 */
public final class ElAccessChainResolver {

    private ElAccessChainResolver() {}

    /**
     * For completion: returns the available properties at the current dot position.
     * E.g., for {@code initiator.<caret>}, returns the ObjectSchema of {@code initiator}.
     */
    public static @Nullable ObjectSchema resolvePropertiesAtCaret(@NotNull ElMemberName memberName) {
        var schemaType = resolveTypeBeforeMember(memberName);
        if (schemaType == null) {
            return null;
        }
        return collectObjectProperties(schemaType);
    }

    /**
     * For references: returns the base variable of the access chain.
     */
    public static @Nullable Variable resolveVariable(@NotNull ElMemberName memberName) {
        var dotSuffix = memberName.getParent();
        if (!(dotSuffix instanceof ElDotSuffix)) {
            return null;
        }

        var accessExpr = dotSuffix.getParent();
        if (!(accessExpr instanceof ElAccessExpr access)) {
            return null;
        }

        return resolveBaseVariable(access, memberName);
    }

    /**
     * Resolves the SchemaType of the expression just before the target member name.
     * Walks the dot-access chain from the base variable through all preceding suffixes.
     */
    private static @Nullable SchemaType resolveTypeBeforeMember(@NotNull ElMemberName memberName) {
        var dotSuffix = memberName.getParent();
        if (!(dotSuffix instanceof ElDotSuffix)) {
            return null;
        }

        var accessExpr = dotSuffix.getParent();
        if (!(accessExpr instanceof ElAccessExpr access)) {
            return null;
        }

        var variable = resolveBaseVariable(access, memberName);
        if (variable == null) {
            return null;
        }

        var currentType = variable.schema().schemaType();

        // Walk suffixes preceding the target
        List<ElSuffix> suffixes = access.getSuffixList();
        for (var suffix : suffixes) {
            if (suffix == dotSuffix) {
                // Reached the target suffix — currentType is the type we need
                break;
            }

            if (!(suffix instanceof ElDotSuffix ds)) {
                // Bracket suffix — can't resolve type through brackets
                return null;
            }

            if (ds.getMemberName() == null) {
                // Incomplete input
                return null;
            }

            if (ds.getArgList() != null) {
                // Method call — return type unknown
                return null;
            }

            var propName = ds.getMemberName().getIdentifier();
            if (propName == null) {
                return null;
            }

            var objectSchema = collectObjectProperties(currentType);
            if (objectSchema == null) {
                return null;
            }

            var prop = objectSchema.properties().get(propName.getText());
            if (prop == null) {
                return null;
            }

            currentType = prop.schemaType();
        }

        return currentType;
    }

    private static @Nullable Variable resolveBaseVariable(@NotNull ElAccessExpr accessExpr, @NotNull PsiElement position) {
        var baseExpr = accessExpr.getExpression();
        if (!(baseExpr instanceof ElIdentifierExpr identExpr)) {
            return null;
        }

        var yamlElement = PsiTreeUtil.getParentOfType(position, YAMLValue.class);
        if (yamlElement == null) {
            return null;
        }

        var varName = identExpr.getIdentifier().getText();
        var variables = VariablesProvider.getVariables(yamlElement);
        for (var variable : variables) {
            if (varName.equals(variable.name())) {
                return variable;
            }
        }

        return null;
    }

    /**
     * Extracts ObjectSchema from a SchemaType.
     * For SchemaType.Object, returns the inner ObjectSchema directly.
     * For SchemaType.Composite, merges ObjectSchema from all Object alternatives.
     * Returns null for all other types (Scalar, Array, Enum, Any).
     */
    static @Nullable ObjectSchema collectObjectProperties(@NotNull SchemaType schemaType) {
        return switch (schemaType) {
            case SchemaType.Object obj -> obj.section();
            case SchemaType.Composite composite -> {
                var merged = new LinkedHashMap<String, SchemaProperty>();
                for (var alt : composite.alternatives()) {
                    if (alt instanceof SchemaType.Object(ObjectSchema section)) {
                        merged.putAll(section.properties());
                    }
                }
                yield merged.isEmpty() ? null : new ObjectSchema(merged, java.util.Set.of(), true);
            }
            default -> null;
        };
    }
}
