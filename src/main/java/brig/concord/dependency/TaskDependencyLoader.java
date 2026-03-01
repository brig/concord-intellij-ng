// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;

final class TaskDependencyLoader {

    private static final Logger LOG = Logger.getInstance(TaskDependencyLoader.class);
    private static final TaskInfoExtractor TASK_INFO_EXTRACTOR = new TaskInfoExtractor();

    private final Project project;

    TaskDependencyLoader(Project project) {
        this.project = project;
    }

    RegistryState load(ProgressIndicator indicator, DependencySyncReporter reporter) {
        var collector = DependencyCollector.getInstance(project);
        var resolver = new DependencyResolver();

        indicator.setText("Collecting dependencies...");
        reporter.reportCollecting();

        // 1. Collect deps
        List<DependencyCollector.ScopeDependencies> scopeDependencies = ReadAction.compute(() -> {
            if (project.isDisposed()) {
                return List.of();
            }
            return collector.collectByScope();
        });

        // 2. Unique Coordinates
        Set<MavenCoordinate> allCoordinates = new LinkedHashSet<>();
        scopeDependencies.forEach(sd -> sd.occurrences().forEach(occ -> allCoordinates.add(occ.coordinate())));

        if (allCoordinates.isEmpty()) {
            reporter.finish(0, 0);
            return RegistryState.EMPTY;
        }

        // 3. Resolve
        var partition = resolveDependencies(allCoordinates, resolver, indicator, reporter);

        // 4. Extract Tasks
        var taskCache = extractTasks(partition.resolvedJars, indicator);

        // 5. Map back to Scopes
        var taskInfosByScope = mapTasksToScopes(scopeDependencies, taskCache);

        // 6. Calculate Problem Files
        var problemFiles = calculateProblemFiles(scopeDependencies, partition.errors);

        reporter.reportErrors(partition.errors, scopeDependencies);
        reporter.finish(partition.resolvedJars.size(), partition.errors.size());

        return new RegistryState(
                taskInfosByScope,
                partition.errors,
                partition.skipped,
                partition.resolvedJars.keySet(),
                problemFiles
        );
    }

    private ResolutionResult resolveDependencies(@NotNull Set<MavenCoordinate> allCoordinates,
                                                 @NotNull DependencyResolver resolver,
                                                 @NotNull ProgressIndicator indicator,
                                                 @NotNull DependencySyncReporter reporter) {

        Set<MavenCoordinate> toResolve = new LinkedHashSet<>();
        Set<MavenCoordinate> skipped = new LinkedHashSet<>();

        for (var coord : allCoordinates) {
            if (coord.isLatestVersion()) {
                skipped.add(coord);
            } else {
                toResolve.add(coord);
            }
        }

        if (toResolve.isEmpty()) {
            return new ResolutionResult(Map.of(), Map.of(), skipped);
        }

        ProgressManager.checkCanceled();

        indicator.setText("Resolving " + toResolve.size() + " artifacts...");
        reporter.reportResolving(toResolve.size());

        var rawResult = resolver.resolveAll(toResolve);

        // Reclassify failed non-version-like coordinates as skipped instead of errors
        Map<MavenCoordinate, String> realErrors = new LinkedHashMap<>();
        for (var entry : rawResult.errors().entrySet()) {
            var coord = entry.getKey();
            var errorMsg = entry.getValue();

            if (coord.isResolvableVersion()) {
                realErrors.put(coord, errorMsg);
            } else {
                skipped.add(coord);
            }
        }

        return new ResolutionResult(rawResult.resolved(), realErrors, skipped);
    }

    private Map<MavenCoordinate, Map<String, TaskInfo>> extractTasks(Map<MavenCoordinate, Path> resolvedJars,
                                                                     ProgressIndicator indicator) {
        Map<MavenCoordinate, Map<String, TaskInfo>> cache = new HashMap<>();

        for (var entry : resolvedJars.entrySet()) {
            ProgressManager.checkCanceled();

            var coord = entry.getKey();
            var jarPath = entry.getValue();

            // Skip if not a JAR (unlikely for Maven coords, but safe check)
            if (!coord.isJar()) {
                continue;
            }

            indicator.setText2("Extracting from " + coord.getArtifactId());

            var extractedTasks = TASK_INFO_EXTRACTOR.extract(jarPath);
            if (!extractedTasks.isEmpty()) {
                cache.put(coord, extractedTasks);
                LOG.debug("Found " + extractedTasks.size() + " tasks in " + coord.getArtifactId());
            }
        }
        return cache;
    }

    private Map<VirtualFile, Map<String, TaskInfo>> mapTasksToScopes(List<DependencyCollector.ScopeDependencies> scopeDependencies,
                                                                     Map<MavenCoordinate, Map<String, TaskInfo>> taskCache) {
        Map<VirtualFile, Map<String, TaskInfo>> result = new HashMap<>();

        for (var scopeDep : scopeDependencies) {
            Map<String, TaskInfo> scopeTasks = new LinkedHashMap<>();

            for (var occ : scopeDep.occurrences()) {
                var tasksFromDep = taskCache.get(occ.coordinate());
                if (tasksFromDep != null) {
                    scopeTasks.putAll(tasksFromDep);
                }
            }

            if (!scopeTasks.isEmpty()) {
                result.put(scopeDep.root().getRootFile(), scopeTasks);
            }
        }
        return result;
    }

    private Set<VirtualFile> calculateProblemFiles(List<DependencyCollector.ScopeDependencies> scopeDependencies,
                                                   Map<MavenCoordinate, String> errors) {
        if (errors.isEmpty()) {
            return Set.of();
        }

        Set<VirtualFile> problemFiles = new LinkedHashSet<>();
        for (var sd : scopeDependencies) {
            for (var occ : sd.occurrences()) {
                if (errors.containsKey(occ.coordinate())) {
                    problemFiles.add(occ.file());
                }
            }
        }
        return problemFiles;
    }

    record ResolutionResult(Map<MavenCoordinate, Path> resolvedJars, Map<MavenCoordinate, String> errors, Set<MavenCoordinate> skipped) {}
}
