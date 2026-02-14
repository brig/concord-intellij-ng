package brig.concord.yaml.meta.model;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLKeyValue;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLValue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class YamlAnyOfType extends YamlComposedTypeBase {

    public static YamlMetaType anyOf(YamlMetaType... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException();
        }
        if (types.length == 1) {
            return types[0];
        }
        return new YamlAnyOfType(flattenTypes(types));
    }

    private static String asTypeName(List<YamlMetaType> types) {
        if (types.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (types.size() == 1) {
            return types.getFirst().getTypeName();
        }
        return types.stream().map(YamlMetaType::getTypeName).distinct().collect(Collectors.joining("|"));
    }

    @Override
    protected YamlMetaType composeTypes(YamlMetaType... types) {
        return anyOf(types);
    }

    protected YamlAnyOfType(List<YamlMetaType> types) {
        super(asTypeName(types), types);
    }

    protected YamlAnyOfType(List<YamlMetaType> types, @NotNull TypeProps props) {
        super(asTypeName(types), types, props);
    }

    protected YamlAnyOfType(YamlMetaType... types) {
        this(Arrays.asList(types));
    }

    @Override
    public @NotNull String getTypeName(@NotNull String suffix) {
        return streamSubTypes()
                .map(t -> t.getTypeName(suffix))
                .distinct()
                .collect(Collectors.joining("|"));
    }

    @Override
    public void validateKey(@NotNull YAMLKeyValue keyValue, @NotNull ProblemsHolder problemsHolder) {
        List<ProblemsHolder> allProblems = allProblemsOrEmpty(problemsHolder, listNonScalarSubTypes(),
                (nextType, nextHolder) -> nextType.validateKey(keyValue, nextHolder));

        allProblems.stream()
                .flatMap(h -> h.getResults().stream())
                .forEach(problemsHolder::registerProblem);
    }

    @Override
    public void validateValue(@NotNull YAMLValue value, @NotNull ProblemsHolder problemsHolder) {
        List<YamlMetaType> types;
        if (value instanceof YAMLScalar) {
            types = listScalarSubTypes();
            if (types.isEmpty()) { // value kind does not match, let some scalar component to report it
                types = firstOrEmpty(listNonScalarSubTypes());
            }
        }
        else {
            types = listNonScalarSubTypes();
            if (types.isEmpty()) {
                // only scalar components, let one of them report it
                types = firstOrEmpty(listScalarSubTypes());
            }
        }
        List<ProblemsHolder> allProblems = allProblemsOrEmpty(problemsHolder, types,
                (nextType, nextHolder) -> nextType.validateValue(value, nextHolder));

        allProblems.stream()
                .flatMap(h -> h.getResults().stream())
                .forEach(problemsHolder::registerProblem);
    }

    @Override
    public @NotNull List<? extends LookupElement> getValueLookups(@NotNull YAMLScalar insertedScalar, @Nullable CompletionContext completionContext) {
        return streamSubTypes()
                .flatMap(type -> type.getValueLookups(insertedScalar, completionContext).stream())
                .collect(Collectors.toList());
    }

    private static List<ProblemsHolder> allProblemsOrEmpty(@NotNull ProblemsHolder problemsHolder, @NotNull List<YamlMetaType> types,
                                                           @NotNull BiConsumer<YamlMetaType, ProblemsHolder> oneValidation) {
        List<ProblemsHolder> problems = new SmartList<>();
        for (YamlMetaType nextType : types) {
            ProblemsHolder nextHolder = makeCopy(problemsHolder);
            oneValidation.accept(nextType, nextHolder);
            if (!nextHolder.hasResults()) {
                return Collections.emptyList();
            }
            problems.add(nextHolder);
        }
        return problems;
    }

    private static @NotNull List<YamlMetaType> firstOrEmpty(@NotNull List<YamlMetaType> list) {
        return list.isEmpty() ? Collections.emptyList() : Collections.singletonList(list.getFirst());
    }
}
