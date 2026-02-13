package brig.concord.meta.model.value;

import brig.concord.ConcordBundle;
import brig.concord.yaml.meta.model.TypeProps;
import brig.concord.yaml.meta.model.YamlAnyOfType;
import brig.concord.yaml.meta.model.YamlMetaType;
import brig.concord.yaml.meta.model.YamlScalarType;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLValue;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AnyOfType extends YamlAnyOfType {

    public static AnyOfType anyOf(YamlMetaType... types) {
        if (types == null || types.length == 0) {
            throw new IllegalArgumentException("AnyOfType.anyOf() requires at least one subtype");
        }

        return new AnyOfType(flattenTypes(types));
    }

    public static AnyOfType anyOf(@NotNull TypeProps props, YamlMetaType... types) {
        if (types == null || types.length == 0) {
            throw new IllegalArgumentException("AnyOfType.anyOf() requires at least one subtype");
        }

        return new AnyOfType(flattenTypes(types), props);
    }

    protected AnyOfType(List<YamlMetaType> types) {
        super(types);
    }

    protected AnyOfType(List<YamlMetaType> types, @NotNull TypeProps props) {
        super(types, props);
    }

    public AnyOfType withProps(@NotNull TypeProps props) {
        return new AnyOfType(streamSubTypes().toList(), props);
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        List<YamlMetaType> candidates = candidateTypesFor(value);

        for (YamlMetaType nextType : candidates) {
            ProblemsHolder nextHolder = makeCopy(problemsHolder);
            nextType.validateValue(value, nextHolder);
            if (!nextHolder.hasResults()) {
                return; // at least one subtype accepts the value
            }
        }

        problemsHolder.registerProblem(
                value,
                ConcordBundle.message("invalid.value", expectedString()),
                ProblemHighlightType.ERROR
        );
    }

    @Override
    public void validateKey(@NotNull YAMLKeyValue keyValue, @NotNull ProblemsHolder problemsHolder) {
        // Keys are always scalar-ish in YAML, but leave it generic: try all subtypes.
        for (YamlMetaType nextType : streamSubTypes().toList()) {
            ProblemsHolder nextHolder = makeCopy(problemsHolder);
            nextType.validateKey(keyValue, nextHolder);
            if (!nextHolder.hasResults()) {
                return; // accepted by at least one subtype
            }
        }

        problemsHolder.registerProblem(
                keyValue,
                ConcordBundle.message("invalid.value", expectedString()),
                ProblemHighlightType.ERROR
        );
    }

    private @NotNull List<YamlMetaType> candidateTypesFor(@NotNull YAMLValue value) {
        List<YamlMetaType> candidates;

        if (value instanceof YAMLScalar) {
            candidates = listScalarSubTypes();
            if (candidates.isEmpty()) {
                // kind mismatch: let one non-scalar type report its "expected kind" error
                candidates = firstOrEmpty(listNonScalarSubTypes());
            }
        } else {
            candidates = listNonScalarSubTypes();
            if (candidates.isEmpty()) {
                // only scalar types exist: let one scalar type report its "expected kind" error
                candidates = firstOrEmpty(listScalarSubTypes());
            }
        }

        if (candidates.isEmpty()) {
            candidates = streamSubTypes().collect(Collectors.toList());
        }
        return candidates;
    }

    private static @NotNull List<YamlMetaType> firstOrEmpty(@NotNull List<YamlMetaType> list) {
        return list.isEmpty() ? Collections.emptyList() : Collections.singletonList(list.getFirst());
    }

    public boolean isScalar() {
        return streamSubTypes().allMatch(t -> t instanceof YamlScalarType);
    }

    public String expectedString() {
        return streamSubTypes()
                .map(YamlMetaType::getTypeName)
                .collect(Collectors.joining("|"));
    }
}
