// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.schema;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record TaskSchemaConditional(
        @NotNull Map<String, List<String>> discriminators,
        @NotNull TaskSchemaSection thenSection
) {
}
