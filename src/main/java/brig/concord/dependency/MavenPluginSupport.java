// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import com.intellij.openapi.diagnostic.Logger;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.aether.ArtifactRepositoryManager;
import org.jetbrains.idea.maven.aether.ProgressConsumer;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Maven support implementation using IntelliJ Maven plugin for artifact download.
 * Repository configuration comes from Concord CLI's mvn.json.
 */
final class MavenPluginSupport extends MavenSupport {

    private static final Logger LOG = Logger.getInstance(MavenPluginSupport.class);

    private final Path myDepsCachePath;
    private final List<MvnJsonConfig.Repository> myRepositories;

    MavenPluginSupport(@NotNull Path depsCachePath, @NotNull List<MvnJsonConfig.Repository> repositories) {
        myDepsCachePath = depsCachePath;
        myRepositories = repositories;
    }

    @Override
    public @NotNull Path getLocalRepositoryPath() {
        return myDepsCachePath;
    }

    @Override
    public @NotNull DependencyResolveResult downloadAll(@NotNull Collection<MavenCoordinate> coordinates) {
        var localRepo = getLocalRepositoryPath();

        try {
            var remoteRepos = buildRemoteRepositories();
            var repoManager = new ArtifactRepositoryManager(
                    localRepo.toFile(), remoteRepos, ProgressConsumer.DEAF);

            Map<MavenCoordinate, Path> resolved = new LinkedHashMap<>();
            Map<MavenCoordinate, String> errors = new LinkedHashMap<>();
            for (var coord : coordinates) {
                if (!coord.isJar()) {
                    continue;
                }

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

        for (var repoConfig : myRepositories) {
            var id = repoConfig.getId();
            var url = repoConfig.getUrl();
            if (id == null || url == null || url.isBlank()) {
                continue;
            }

            var builder = new RemoteRepository.Builder(id, "default", url);

            var release = repoConfig.getReleasePolicy();
            if (release != null) {
                builder.setReleasePolicy(new RepositoryPolicy(
                        release.isEnabled(),
                        release.getUpdatePolicy(),
                        release.getChecksumPolicy()));
            }

            var snapshot = repoConfig.getSnapshotPolicy();
            if (snapshot != null) {
                builder.setSnapshotPolicy(new RepositoryPolicy(
                        snapshot.isEnabled(),
                        snapshot.getUpdatePolicy(),
                        snapshot.getChecksumPolicy()));
            }

            var auth = repoConfig.getAuth();
            if (auth != null && (auth.getUsername() != null || auth.getPassword() != null)) {
                var authBuilder = new AuthenticationBuilder();
                if (auth.getUsername() != null) {
                    authBuilder.addUsername(auth.getUsername());
                }
                if (auth.getPassword() != null) {
                    authBuilder.addPassword(auth.getPassword());
                }
                builder.setAuthentication(authBuilder.build());
            }

            var proxyConfig = repoConfig.getProxy();
            if (proxyConfig != null && proxyConfig.getHost() != null) {
                var port = proxyConfig.getPort() != null ? proxyConfig.getPort() : 8080;
                builder.setProxy(new Proxy(proxyConfig.getType(), proxyConfig.getHost(), port));
            }

            repos.add(builder.build());
        }

        return repos;
    }
}
