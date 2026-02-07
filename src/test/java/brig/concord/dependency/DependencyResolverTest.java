package brig.concord.dependency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DependencyResolverTest {

    private static final MavenCoordinate COORD =
            MavenCoordinate.parse("mvn://com.example:my-task:1.0.0");

    @Test
    void resolvesFromLocalRepo(@TempDir Path tempDir) throws IOException {
        var jarPath = tempDir.resolve(COORD.getRepositoryPath());
        Files.createDirectories(jarPath.getParent());
        Files.writeString(jarPath, "fake-jar");

        var resolver = new DependencyResolver(stubMavenSupport(tempDir, null));
        var result = resolver.resolve(COORD);

        assertEquals(jarPath, result);
    }

    @Test
    void downloadsWhenNotLocal(@TempDir Path tempDir) throws IOException {
        var downloadedJar = tempDir.resolve("downloaded.jar");
        Files.writeString(downloadedJar, "downloaded-fake-jar");

        var resolver = new DependencyResolver(stubMavenSupport(tempDir, downloadedJar));
        var result = resolver.resolve(COORD);

        assertEquals(downloadedJar, result);
    }

    @Test
    void returnsNullWhenLocalRepoUnavailable() {
        var resolver = new DependencyResolver(stubMavenSupport(null, null));
        var result = resolver.resolve(COORD);

        assertNull(result);
    }

    @Test
    void returnsNullWhenDownloadFails(@TempDir Path tempDir) {
        var resolver = new DependencyResolver(stubMavenSupport(tempDir, null));
        var result = resolver.resolve(COORD);

        assertNull(result);
    }

    @Test
    void resolveAllReturnsOnlyResolved(@TempDir Path tempDir) throws IOException {
        var coord1 = MavenCoordinate.parse("mvn://com.example:task-a:1.0.0");
        var coord2 = MavenCoordinate.parse("mvn://com.example:task-b:1.0.0");

        // Only coord1 exists locally
        var jarPath = tempDir.resolve(coord1.getRepositoryPath());
        Files.createDirectories(jarPath.getParent());
        Files.writeString(jarPath, "fake-jar");

        var resolver = new DependencyResolver(stubMavenSupport(tempDir, null));
        var result = resolver.resolveAll(List.of(coord1, coord2));

        assertEquals(1, result.size());
        assertEquals(jarPath, result.get(coord1));
        assertNull(result.get(coord2));
    }

    @Test
    void resolveAllEmptyInput() {
        var resolver = new DependencyResolver(stubMavenSupport(null, null));
        var result = resolver.resolveAll(List.of());

        assertTrue(result.isEmpty());
    }

    private static MavenSupport stubMavenSupport(@Nullable Path localRepo, @Nullable Path downloadResult) {
        return new MavenSupport() {
            @Override
            public boolean isAvailable() {
                return false;
            }

            @Override
            public @Nullable Path getLocalRepositoryPath() {
                return localRepo;
            }

            @Override
            public @NotNull List<String> getRemoteRepositoryUrls() {
                return List.of();
            }

            @Override
            public @Nullable Path downloadArtifact(@NotNull MavenCoordinate coordinate) {
                return downloadResult;
            }
        };
    }
}