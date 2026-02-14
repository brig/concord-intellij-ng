package brig.concord.yaml.meta.model;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.util.ArrayUtilRt;
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
    private String[] myLiterals = ArrayUtilRt.EMPTY_STRING_ARRAY;
    private String[] myDescriptions = ArrayUtilRt.EMPTY_STRING_ARRAY;

    public YamlEnumType(@NotNull String typeName) {
        super(typeName);
    }

    public YamlEnumType(@NotNull String typeName, @NotNull TypeProps props) {
        super(typeName, props);
    }

    public String[] getLiterals() {
        return myLiterals;
    }

    public void setLiterals(String... literals) {
        myLiterals = cloneArray(literals);
        checkDescriptionsMatchLiterals();
    }

    public YamlEnumType withLiterals(String... literals) {
        setLiterals(literals);
        return this;
    }

    public void setDescriptionKeys(@NotNull @PropertyKey(resourceBundle = BUNDLE) String... keys) {
        this.myDescriptions = new String[keys.length];
        for (var i = 0; i < keys.length; i++) {
            var key = keys[i];
            this.myDescriptions[i] = ConcordBundle.message(key);
        }
        checkDescriptionsMatchLiterals();
    }

    public YamlEnumType withDescriptions(@NotNull String... descriptions) {
        this.myDescriptions = cloneArray(descriptions);
        checkDescriptionsMatchLiterals();
        return this;
    }

    public String[] getLiteralDescriptions() {
        return myDescriptions;
    }

    @Override
    public @NotNull List<DocumentedField> getValues() {
        var descriptions = getLiteralDescriptions();
        if (descriptions.length > 0) {
            var literals = getLiterals();
            var children = new ArrayList<DocumentedField>(literals.length);
            for (var i = 0; i < literals.length; i++) {
                children.add(new DocumentedField(literals[i], getTypeName(), false, descriptions[i], List.of()));
            }
            return children;
        }
        return List.of();
    }

    protected final @NotNull Stream<String> getLiteralsStream() {
        return Arrays.stream(myLiterals);
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
        return Arrays.stream(myLiterals).map((String literal) -> createValueLookup(literal, false))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    protected @Nullable LookupElement createValueLookup(@NotNull String literal, boolean deprecated) {
        return LookupElementBuilder.create(literal).withStrikeoutness(deprecated);
    }


    private void checkDescriptionsMatchLiterals() {
        if (myDescriptions.length != 0 && myLiterals.length != 0 && myDescriptions.length != myLiterals.length) {
            throw new IllegalStateException(
                    "descriptions count (" + myDescriptions.length + ") != literals count (" + myLiterals.length + ") in " + getTypeName());
        }
    }

    private static String @NotNull [] cloneArray(String @NotNull [] array) {
        return array.length == 0 ? ArrayUtilRt.EMPTY_STRING_ARRAY : array.clone();
    }
}
