// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.meta.model;

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
