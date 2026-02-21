// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.dependency;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Result of dependency resolution containing both resolved artifacts and errors.
 *
 * @param resolved successfully resolved coordinates mapped to their local JAR paths
 * @param errors   coordinates that failed to resolve mapped to error messages
 */
public record DependencyResolveResult(
        @NotNull Map<MavenCoordinate, Path> resolved,
        @NotNull Map<MavenCoordinate, String> errors
) {

    public static DependencyResolveResult allFailed(@NotNull Collection<MavenCoordinate> coordinates,
                                                     @NotNull String message) {
        Map<MavenCoordinate, String> errors = new LinkedHashMap<>();
        for (var coord : coordinates) {
            errors.put(coord, message);
        }
        return new DependencyResolveResult(Map.of(), errors);
    }
}