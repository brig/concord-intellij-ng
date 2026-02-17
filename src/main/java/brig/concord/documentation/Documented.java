// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.documentation;

import brig.concord.yaml.psi.YAMLKeyValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface Documented {

    static Documented ofDescription(@NotNull String description) {
        return new Documented() {
            @Override
            public @NotNull String getDescription() {
                return description;
            }
        };
    }

    default @Nullable String getDescription() {
        return null;
    }

    default @Nullable String getDocumentationDescription(@NotNull YAMLKeyValue element) {
        return getDescription();
    }

    default @Nullable String getDocumentationExample() {
        return null;
    }

    default @NotNull List<DocumentedField> getDocumentationFields() {
        return List.of();
    }

    default @NotNull List<DocumentedField> getValues() {
        return List.of();
    }

    default @NotNull List<DocumentedSection> getDocumentationSections() {
        return List.of();
    }

    default @Nullable String getDocumentationFooter() {
        return null;
    }

    record DocumentedField(String name, @Nullable String typeDisplayName, boolean required,
                           @Nullable String description, @NotNull List<DocumentedField> children) {
    }

    record DocumentedSection(String title, List<DocumentedField> fields) {
    }
}
