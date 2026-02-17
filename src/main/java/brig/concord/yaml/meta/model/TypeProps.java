// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.yaml.meta.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import static brig.concord.ConcordBundle.BUNDLE;

public record TypeProps(
        @Nullable @PropertyKey(resourceBundle = BUNDLE) String descriptionKey,
        String description,
        boolean isRequired
) {

    public static TypeProps descKey(@NotNull @PropertyKey(resourceBundle = BUNDLE) String descriptionKey) {
        return new TypeProps(descriptionKey, null, false);
    }

    public static TypeProps desc(@Nullable String description) {
        return new TypeProps(null, description, false);
    }

    public static TypeProps required() {
        return new TypeProps(null, null, true);
    }

    public TypeProps andRequired() {
        return new TypeProps(descriptionKey, description, true);
    }

    public TypeProps andRequired(boolean required) {
        return new TypeProps(descriptionKey, description, required);
    }
}
