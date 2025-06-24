package brig.concord.yaml.meta.model;

// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

import org.jetbrains.annotations.NotNull;

public class TypeFieldPair {
    private final @NotNull Field myField;
    private final @NotNull YamlMetaType myOwnerClass;

    public TypeFieldPair(@NotNull YamlMetaType ownerClass, @NotNull Field field) {
        myField = field;
        myOwnerClass = ownerClass;
    }

    public @NotNull YamlMetaType getMetaType() {
        return myOwnerClass;
    }

    public @NotNull Field getField() {
        return myField;
    }
}
