// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Resolves Maven dependencies to local JAR files.
 */
public final class DependencyResolver {

    private static final Logger LOG = Logger.getInstance(DependencyResolver.class);

    private final MavenSupport mavenSupport;

    public DependencyResolver() {
        this.mavenSupport = MavenSupport.create();
    }

    DependencyResolver(@NotNull MavenSupport mavenSupport) {
        this.mavenSupport = mavenSupport;
    }

    /**
     * Resolves multiple dependencies to local JAR paths, collecting error messages for failures.
     *
     * @return result containing resolved JARs and error messages for failed coordinates
     */
    public @NotNull DependencyResolveResult resolveAll(@NotNull Collection<MavenCoordinate> coordinates) {
        var localRepo = mavenSupport.getLocalRepositoryPath();
        if (localRepo == null) {
            return DependencyResolveResult.allFailed(coordinates, "Cannot determine local Maven repository path");
        }

        Map<MavenCoordinate, Path> resolved = new LinkedHashMap<>();
        List<MavenCoordinate> toDownload = new ArrayList<>();

        for (var coord : coordinates) {
            var jarPath = localRepo.resolve(coord.getRepositoryPath());
            if (Files.isRegularFile(jarPath)) {
                resolved.put(coord, jarPath);
            } else {
                toDownload.add(coord);
            }
        }

        if (!toDownload.isEmpty()) {
            LOG.debug("Downloading " + toDownload.size() + " artifacts not found locally");
            var downloadResult = mavenSupport.downloadAll(toDownload);
            resolved.putAll(downloadResult.resolved());
            return new DependencyResolveResult(resolved, downloadResult.errors());
        }

        return new DependencyResolveResult(resolved, Map.of());
    }

}
