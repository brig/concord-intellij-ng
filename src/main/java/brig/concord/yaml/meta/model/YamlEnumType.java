// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.meta.model;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.ConcordBundle;
import brig.concord.yaml.psi.YAMLScalar;
import org.jetbrains.annotations.PropertyKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static brig.concord.ConcordBundle.BUNDLE;

public class YamlEnumType extends YamlScalarType {

    public record EnumValue(@NotNull String literal, @Nullable String description) {

        public EnumValue(@NotNull String literal) {
            this(literal, null);
        }

        public static EnumValue ofKey(@NotNull String literal,
                                      @NotNull @PropertyKey(resourceBundle = BUNDLE) String descriptionKey) {
            return new EnumValue(literal, ConcordBundle.message(descriptionKey));
        }

        public static List<EnumValue> fromLiterals(String... literals) {
            return Arrays.stream(literals).map(EnumValue::new).toList();
        }

        public static List<EnumValue> fromLiterals(List<String> literals, List<String> descriptions) {
            if (descriptions.isEmpty()) {
                return literals.stream().map(EnumValue::new).toList();
            }
            if (descriptions.size() != literals.size()) {
                throw new IllegalArgumentException(
                        "descriptions count (" + descriptions.size() + ") != literals count (" + literals.size() + ")");
            }
            var result = new ArrayList<EnumValue>(literals.size());
            for (int i = 0; i < literals.size(); i++) {
                result.add(new EnumValue(literals.get(i), descriptions.get(i)));
            }
            return List.copyOf(result);
        }
    }

    private final List<EnumValue> myEnumValues;

    public YamlEnumType(@NotNull String typeName) {
        super(typeName);
        myEnumValues = List.of();
    }

    public YamlEnumType(@NotNull String typeName, @NotNull List<EnumValue> enumValues) {
        super(typeName);
        myEnumValues = List.copyOf(enumValues);
    }

    public YamlEnumType(@NotNull String typeName, @NotNull TypeProps props) {
        super(typeName, props);
        myEnumValues = List.of();
    }

    public YamlEnumType(@NotNull String typeName, @NotNull TypeProps props, @NotNull List<EnumValue> enumValues) {
        super(typeName, props);
        myEnumValues = List.copyOf(enumValues);
    }

    public List<EnumValue> getEnumValues() {
        return myEnumValues;
    }

    @Override
    public @NotNull List<DocumentedField> getValues() {
        if (myEnumValues.isEmpty() || myEnumValues.getFirst().description() == null) {
            return List.of();
        }
        return myEnumValues.stream()
                .map(v -> new DocumentedField(v.literal(), getTypeName(), false, v.description(), List.of()))
                .toList();
    }

    protected final @NotNull Stream<String> getLiteralsStream() {
        return myEnumValues.stream().map(EnumValue::literal);
    }

    @Override
    protected void validateScalarValue(@NotNull YAMLScalar scalarValue, @NotNull ProblemsHolder holder) {
        super.validateScalarValue(scalarValue, holder);

        var text = scalarValue.getTextValue();
        if (text.isEmpty()) {
            // not our business
            return;
        }

        if (getLiteralsStream().noneMatch(text::equals)) {
            //TODO quickfix makes sense here if !text.equals(text.toLowerCase)
            holder.registerProblem(scalarValue,
                    ConcordBundle.message("YamlEnumType.validation.error.value.unknown", text),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
    }

    @Override
    public @NotNull List<LookupElement> getValueLookups(@NotNull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        return myEnumValues.stream().map((EnumValue v) -> createValueLookup(v.literal(), false))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected @Nullable LookupElement createValueLookup(@NotNull String literal, boolean deprecated) {
        return LookupElementBuilder.create(literal).withStrikeoutness(deprecated);
    }
}
