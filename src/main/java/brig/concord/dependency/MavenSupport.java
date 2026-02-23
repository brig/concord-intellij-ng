// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;

/**
 * Provides access to Maven configuration.
 * Uses Concord CLI's mvn.json for repository configuration and depsCache for local storage.
 * Falls back when Maven plugin is not available.
 */
public abstract class MavenSupport {

    private static final Logger LOG = Logger.getInstance(MavenSupport.class);

    public static @NotNull MavenSupport create() {
        var repoSettings = ConcordRepositorySettings.getInstance();
        var depsCachePath = repoSettings.getEffectiveDepsCachePath();
        var mvnJsonPath = repoSettings.getEffectiveMvnJsonPath();

        List<MvnJsonConfig.Repository> repositories;
        try {
            var config = MvnJsonParser.read(mvnJsonPath);
            repositories = config.getRepositories();
        } catch (Exception e) {
            LOG.warn("Failed to read mvn.json from " + mvnJsonPath, e);
            repositories = List.of();
        }

        if (repositories.isEmpty()) {
            repositories = List.of(MvnJsonConfig.mavenCentral());
        }

        if (isMavenPluginAvailable()) {
            try {
                return new MavenPluginSupport(depsCachePath, repositories);
            } catch (Exception e) {
                LOG.warn("Failed to initialize Maven plugin support", e);
            }
        }
        return new FallbackMavenSupport(depsCachePath);
    }

    private static boolean isMavenPluginAvailable() {
        try {
            Class.forName("org.jetbrains.idea.maven.project.MavenProjectsManager");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public abstract @Nullable Path getLocalRepositoryPath();

    public abstract @NotNull DependencyResolveResult downloadAll(@NotNull Collection<MavenCoordinate> coordinates);

    private static class FallbackMavenSupport extends MavenSupport {

        private final Path myDepsCachePath;

        FallbackMavenSupport(@NotNull Path depsCachePath) {
            myDepsCachePath = depsCachePath;
        }

        @Override
        public @Nullable Path getLocalRepositoryPath() {
            return myDepsCachePath;
        }

        @Override
        public @NotNull DependencyResolveResult downloadAll(@NotNull Collection<MavenCoordinate> coordinates) {
            return DependencyResolveResult.allFailed(coordinates, "Maven plugin is not available");
        }
    }
}
