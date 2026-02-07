package brig.concord.dependency;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.aether.ArtifactRepositoryManager;
import org.jetbrains.idea.maven.aether.ProgressConsumer;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maven support implementation using IntelliJ Maven plugin.
 */
final class MavenPluginSupport extends MavenSupport {

    private static final Logger LOG = Logger.getInstance(MavenPluginSupport.class);

    private final Project project;

    MavenPluginSupport(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public @Nullable Path getLocalRepositoryPath() {
        var manager = MavenProjectsManager.getInstance(project);
        var localRepoPath = manager.getRepositoryPath();
        if (localRepoPath != null) {
            return localRepoPath;
        }

        // Fallback to default
        return Path.of(System.getProperty("user.home"), ".m2", "repository");
    }

    @Override
    public @NotNull List<String> getRemoteRepositoryUrls() {
        var manager = MavenProjectsManager.getInstance(project);
        var repos = manager.getRemoteRepositories();

        List<String> urls = new ArrayList<>();
        for (var repo : repos) {
            var url = repo.getUrl();
            if (url != null && !url.isBlank()) {
                urls.add(url);
            }
        }

        // Always include Maven Central as fallback
        if (urls.isEmpty()) {
            urls.add("https://repo.maven.apache.org/maven2");
        }

        return urls;
    }

    @Override
    public @Nullable Path downloadArtifact(@NotNull MavenCoordinate coordinate) {
        var localRepo = getLocalRepositoryPath();
        if (localRepo == null) {
            return null;
        }

        try {
            var remoteRepos = buildRemoteRepositories();
            var repoManager = new ArtifactRepositoryManager(
                    localRepo.toFile(),
                    remoteRepos,
                    ProgressConsumer.DEAF
            );

            var files = repoManager.resolveDependency(
                    coordinate.getGroupId(),
                    coordinate.getArtifactId(),
                    coordinate.getVersion(),
                    false,  // no transitive dependencies
                    List.of()
            );

            if (!files.isEmpty()) {
                var file = files.iterator().next();
                LOG.info("Downloaded artifact: " + coordinate + " -> " + file);
                return file.toPath();
            }
        } catch (Exception e) {
            LOG.warn("Failed to download artifact: " + coordinate, e);
        }

        return null;
    }

    @Override
    public @NotNull DependencyResolveResult downloadAll(@NotNull Collection<MavenCoordinate> coordinates) {
        var localRepo = getLocalRepositoryPath();
        if (localRepo == null) {
            return DependencyResolveResult.allFailed(coordinates, "Cannot determine local Maven repository path");
        }

        try {
            var remoteRepos = buildRemoteRepositories();
            var repoManager = new ArtifactRepositoryManager(
                    localRepo.toFile(), remoteRepos, ProgressConsumer.DEAF);

            Map<MavenCoordinate, Path> resolved = new LinkedHashMap<>();
            Map<MavenCoordinate, String> errors = new LinkedHashMap<>();
            for (var coord : coordinates) {
                try {
                    var files = repoManager.resolveDependency(
                            coord.getGroupId(), coord.getArtifactId(), coord.getVersion(),
                            false, List.of());
                    if (!files.isEmpty()) {
                        resolved.put(coord, files.iterator().next().toPath());
                    } else {
                        errors.put(coord, "Artifact not found");
                    }
                } catch (Exception e) {
                    LOG.warn("Failed to download artifact: " + coord, e);
                    errors.put(coord, e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                }
            }
            return new DependencyResolveResult(resolved, errors);
        } catch (Exception e) {
            LOG.warn("Failed to initialize repository manager", e);
            var msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return DependencyResolveResult.allFailed(coordinates, msg);
        }
    }

    private @NotNull List<RemoteRepository> buildRemoteRepositories() {
        List<RemoteRepository> repos = new ArrayList<>();

        for (var url : getRemoteRepositoryUrls()) {
            repos.add(ArtifactRepositoryManager.createRemoteRepository("repo-" + repos.size(), url));
        }

        return repos;
    }
}
