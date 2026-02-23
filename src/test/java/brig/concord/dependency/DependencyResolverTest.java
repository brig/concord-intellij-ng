// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DependencyResolverTest {

    private static final @NotNull MavenCoordinate COORD =
            Objects.requireNonNull(MavenCoordinate.parse("mvn://com.example:my-task:1.0.0"));

    @Test
    void resolvesFromLocalRepo(@TempDir Path tempDir) throws IOException {
        var jarPath = tempDir.resolve(COORD.getRepositoryPath());
        Files.createDirectories(jarPath.getParent());
        Files.writeString(jarPath, "fake-jar");

        var resolver = new DependencyResolver(stubMavenSupport(tempDir, null));
        var result = resolver.resolveAll(List.of(COORD));

        assertEquals(1, result.resolved().size());
        assertEquals(jarPath, result.resolved().get(COORD));
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void downloadsWhenNotLocal(@TempDir Path tempDir) throws IOException {
        var downloadedJar = tempDir.resolve("downloaded.jar");
        Files.writeString(downloadedJar, "downloaded-fake-jar");

        var resolver = new DependencyResolver(stubMavenSupport(tempDir, Map.of(COORD, downloadedJar)));
        var result = resolver.resolveAll(List.of(COORD));

        assertEquals(1, result.resolved().size());
        assertEquals(downloadedJar, result.resolved().get(COORD));
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void returnsErrorsWhenLocalRepoUnavailable() {
        var resolver = new DependencyResolver(stubMavenSupport(null, null));
        var result = resolver.resolveAll(List.of(COORD));

        assertTrue(result.resolved().isEmpty());
        assertEquals(1, result.errors().size());
        assertNotNull(result.errors().get(COORD));
    }

    @Test
    void returnsErrorsWhenDownloadFails(@TempDir Path tempDir) {
        var resolver = new DependencyResolver(stubMavenSupport(tempDir, null));
        var result = resolver.resolveAll(List.of(COORD));

        assertTrue(result.resolved().isEmpty());
        assertEquals(1, result.errors().size());
        assertNotNull(result.errors().get(COORD));
    }

    @Test
    void resolveAllReturnsResolvedAndErrors(@TempDir Path tempDir) throws IOException {
        var coord1 = MavenCoordinate.parse("mvn://com.example:task-a:1.0.0");
        Assertions.assertNotNull(coord1);
        var coord2 = MavenCoordinate.parse("mvn://com.example:task-b:1.0.0");
        Assertions.assertNotNull(coord2);

        // Only coord1 exists locally
        var jarPath = tempDir.resolve(coord1.getRepositoryPath());
        Files.createDirectories(jarPath.getParent());
        Files.writeString(jarPath, "fake-jar");

        var resolver = new DependencyResolver(stubMavenSupport(tempDir, null));
        var result = resolver.resolveAll(List.of(coord1, coord2));

        assertEquals(1, result.resolved().size());
        assertEquals(jarPath, result.resolved().get(coord1));
        assertFalse(result.errors().isEmpty());
        assertNotNull(result.errors().get(coord2));
    }

    @Test
    void resolveAllEmptyInput() {
        var resolver = new DependencyResolver(stubMavenSupport(null, null));
        var result = resolver.resolveAll(List.of());

        assertTrue(result.resolved().isEmpty());
        assertTrue(result.errors().isEmpty());
    }

    private static MavenSupport stubMavenSupport(@Nullable Path localRepo,
                                                  @Nullable Map<MavenCoordinate, Path> downloadResults) {
        return new MavenSupport() {
            @Override
            public @Nullable Path getLocalRepositoryPath() {
                return localRepo;
            }

            @Override
            public @NotNull DependencyResolveResult downloadAll(@NotNull Collection<MavenCoordinate> coordinates) {
                Map<MavenCoordinate, Path> resolved = new LinkedHashMap<>();
                Map<MavenCoordinate, String> errors = new LinkedHashMap<>();
                for (var coord : coordinates) {
                    Path path = downloadResults != null ? downloadResults.get(coord) : null;
                    if (path != null) {
                        resolved.put(coord, path);
                    } else {
                        errors.put(coord, "Download failed");
                    }
                }
                return new DependencyResolveResult(resolved, errors);
            }
        };
    }
}
