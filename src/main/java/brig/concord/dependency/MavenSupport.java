package brig.concord.dependency;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

/**
 * Provides access to Maven configuration.
 * Handles the case when Maven plugin is not installed.
 */
public abstract class MavenSupport {

    private static final Logger LOG = Logger.getInstance(MavenSupport.class);

    /**
     * Creates MavenSupport instance.
     * Returns Maven plugin-based implementation if available, otherwise fallback.
     */
    public static @NotNull MavenSupport create(@NotNull Project project) {
        if (isMavenPluginAvailable()) {
            try {
                return new MavenPluginSupport(project);
            } catch (Exception e) {
                LOG.warn("Failed to initialize Maven plugin support", e);
            }
        }
        return new FallbackMavenSupport();
    }

    private static boolean isMavenPluginAvailable() {
        try {
            Class.forName("org.jetbrains.idea.maven.project.MavenProjectsManager");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Returns true if Maven plugin support is available.
     */
    public abstract boolean isAvailable();

    /**
     * Returns path to local Maven repository.
     */
    public abstract @Nullable Path getLocalRepositoryPath();

    /**
     * Returns list of remote repository URLs.
     */
    public abstract @NotNull List<String> getRemoteRepositoryUrls();

    /**
     * Downloads an artifact and returns path to the downloaded file.
     *
     * @return path to downloaded JAR or null if download failed
     */
    public abstract @Nullable Path downloadArtifact(@NotNull MavenCoordinate coordinate);

    /**
     * Downloads multiple artifacts, collecting both results and error messages.
     */
    public @NotNull DependencyResolveResult downloadAll(@NotNull Collection<MavenCoordinate> coordinates) {
        Map<MavenCoordinate, Path> resolved = new LinkedHashMap<>();
        Map<MavenCoordinate, String> errors = new LinkedHashMap<>();
        for (var coord : coordinates) {
            var path = downloadArtifact(coord);
            if (path != null) {
                resolved.put(coord, path);
            } else {
                errors.put(coord, "Download failed");
            }
        }
        return new DependencyResolveResult(resolved, errors);
    }

    /**
     * Fallback implementation when Maven plugin is not available.
     */
    private static class FallbackMavenSupport extends MavenSupport {

        private static final Path DEFAULT_LOCAL_REPO = Path.of(
                System.getProperty("user.home"), ".m2", "repository"
        );

        @Override
        public boolean isAvailable() {
            return false;
        }

        @Override
        public @Nullable Path getLocalRepositoryPath() {
            return DEFAULT_LOCAL_REPO;
        }

        @Override
        public @NotNull List<String> getRemoteRepositoryUrls() {
            return List.of("https://repo.maven.apache.org/maven2");
        }

        @Override
        public @Nullable Path downloadArtifact(@NotNull MavenCoordinate coordinate) {
            // Cannot download without Maven plugin
            LOG.info("Maven plugin not available, cannot download: " + coordinate);
            return null;
        }

        @Override
        public @NotNull DependencyResolveResult downloadAll(@NotNull Collection<MavenCoordinate> coordinates) {
            return DependencyResolveResult.allFailed(coordinates, "Maven plugin is not available");
        }
    }
}
