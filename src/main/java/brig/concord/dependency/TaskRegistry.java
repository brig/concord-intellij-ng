// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import brig.concord.psi.ConcordScopeService;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Project service that manages task names extracted from dependencies.
 * Provides task names for autocomplete in the task: field.
 */
@Service(Service.Level.PROJECT)
public final class TaskRegistry implements Disposable {

    private static final Logger LOG = Logger.getInstance(TaskRegistry.class);
    private static final TaskNameExtractor TASK_NAME_EXTRACTOR = new TaskNameExtractor();

    private final Project project;
    private final AtomicBoolean reloading = new AtomicBoolean(false);

    // Task names grouped by scope (root file). Swapped atomically to avoid read-during-update races.
    private volatile Map<VirtualFile, Set<String>> taskNamesByScope = Map.of();

    // Dependencies that failed to resolve, with error messages. Swapped atomically.
    private volatile Map<MavenCoordinate, String> failedDependencies = Map.of();

    // Dependencies skipped because their version is not resolvable locally (e.g. "latest", "PROJECT_VERSION").
    private volatile Set<MavenCoordinate> skippedDependencies = Set.of();

    // Files currently marked as having problems in the Project tree via WolfTheProblemSolver.
    private static final Object DEPENDENCY_PROBLEM_SOURCE = new Object();
    private volatile Set<VirtualFile> markedProblemFiles = Set.of();

    public TaskRegistry(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull TaskRegistry getInstance(@NotNull Project project) {
        return project.getService(TaskRegistry.class);
    }

    /**
     * Returns dependencies that failed to resolve, mapped to error messages.
     */
    public @NotNull Map<MavenCoordinate, String> getFailedDependencies() {
        return failedDependencies;
    }

    /**
     * Returns dependencies that were skipped because their version is not resolvable locally.
     */
    public @NotNull Set<MavenCoordinate> getSkippedDependencies() {
        return skippedDependencies;
    }

    /**
     * Returns task names available in the scope of the given element.
     */
    public @NotNull Set<String> getTaskNames(@NotNull PsiElement context) {
        var psiFile = context.getContainingFile();
        if (psiFile == null) {
            return Set.of();
        }

        var virtualFile = psiFile.getOriginalFile().getVirtualFile();
        if (virtualFile == null) {
            return Set.of();
        }

        var scopeService = ConcordScopeService.getInstance(project);
        var scopes = scopeService.getScopesForFile(virtualFile);

        if (scopes.isEmpty()) {
            return Set.of();
        }

        // Collect task names from all matching scopes
        Set<String> result = new LinkedHashSet<>();
        for (var scope : scopes) {
            var tasks = taskNamesByScope.get(scope.getRootFile());
            if (tasks != null) {
                result.addAll(tasks);
            }
        }

        return result;
    }

    /**
     * Sets failed dependencies directly. For testing only.
     */
    @TestOnly
    public void setFailedDependencies(@NotNull Map<MavenCoordinate, String> failures) {
        this.failedDependencies = failures.isEmpty() ? Map.of() : Map.copyOf(failures);
    }

    /**
     * Sets skipped dependencies directly. For testing only.
     */
    @TestOnly
    public void setSkippedDependencies(@NotNull Set<MavenCoordinate> skipped) {
        this.skippedDependencies = skipped.isEmpty() ? Set.of() : Set.copyOf(skipped);
    }

    /**
     * Sets task names directly. For testing only.
     */
    @TestOnly
    public void setTaskNames(@NotNull VirtualFile scopeRoot, @NotNull Set<String> taskNames) {
        var copy = new HashMap<>(taskNamesByScope);
        copy.put(scopeRoot, new LinkedHashSet<>(taskNames));
        taskNamesByScope = Map.copyOf(copy);
    }

    /**
     * Triggers background reload of task names from dependencies.
     * Used when user explicitly requests reload (e.g., via Refresh button).
     */
    public void reload() {
        if (!reloading.compareAndSet(false, true)) {
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading Concord tasks", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    var modCountAtStart = DependencyChangeTracker.getInstance(project).getCurrentModCount();
                    indicator.setIndeterminate(false);
                    loadTaskNames(indicator, modCountAtStart, false);
                } finally {
                    reloading.set(false);
                }
            }
        });
    }

    /**
     * Triggers initial background load of task names.
     * Used on project startup to establish baseline state.
     */
    public void initialLoad() {
        if (!reloading.compareAndSet(false, true)) {
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading Concord tasks", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    indicator.setIndeterminate(false);
                    loadTaskNames(indicator, -1, true);
                } finally {
                    reloading.set(false);
                }
            }
        });
    }

    /**
     * Loads task names from dependencies.
     *
     * @param indicator       progress indicator
     * @param modCountAtStart modification count at start (ignored for initial load)
     * @param isInitialLoad   true if this is initial load (no prior state)
     */
    private void loadTaskNames(@NotNull ProgressIndicator indicator,
                               long modCountAtStart,
                               boolean isInitialLoad) {
        if (project.isDisposed()) {
            return;
        }

        LOG.debug("Loading task names from dependencies (initial=" + isInitialLoad + ")...");

        var reporter = new DependencySyncReporter(project);
        reporter.start();

        try {
            loadTaskNamesImpl(indicator, modCountAtStart, isInitialLoad, reporter);
        } catch (Exception e) {
            reporter.finish(0, 1);
            throw e;
        }
    }

    private void loadTaskNamesImpl(@NotNull ProgressIndicator indicator,
                                   long modCountAtStart,
                                   boolean isInitialLoad,
                                   @NotNull DependencySyncReporter reporter) {
        var collector = DependencyCollector.getInstance(project);
        var resolver = new DependencyResolver(project);

        indicator.setText("Collecting dependencies...");
        reporter.reportCollecting();
        var scopeDependencies = ReadAction.compute(() -> {
            if (project.isDisposed()) {
                return List.<DependencyCollector.ScopeDependencies>of();
            }
            return collector.collectByScope();
        });

        if (scopeDependencies.isEmpty()) {
            LOG.debug("No scopes found");
            skippedDependencies = Set.of();
            updateFailedAndRestart(Map.of(), List.of());
            notifyTracker(Set.of(), modCountAtStart, isInitialLoad);
            reporter.finish(0, 0);
            return;
        }

        // Step 1: Collect all unique coordinates across all scopes
        Set<MavenCoordinate> allCoordinates = new LinkedHashSet<>();
        for (var scopeDep : scopeDependencies) {
            for (var occ : scopeDep.occurrences()) {
                allCoordinates.add(occ.coordinate());
            }
        }

        if (allCoordinates.isEmpty()) {
            LOG.debug("No dependencies found");
            skippedDependencies = Set.of();
            updateFailedAndRestart(Map.of(), scopeDependencies);
            notifyTracker(Set.of(), modCountAtStart, isInitialLoad);
            reporter.finish(0, 0);
            return;
        }

        LOG.debug("Found " + allCoordinates.size() + " unique dependencies across " + scopeDependencies.size() + " scopes");

        ProgressManager.checkCanceled();

        // Step 2: Partition coordinates — skip "latest" versions, try to resolve the rest
        var partitioned = resolvePartitioned(allCoordinates, resolver, indicator, reporter);

        ProgressManager.checkCanceled();

        skippedDependencies = partitioned.skipped().isEmpty() ? Set.of() : Set.copyOf(partitioned.skipped());
        updateFailedAndRestart(partitioned.errors(), scopeDependencies);
        reporter.reportErrors(partitioned.errors(), scopeDependencies);

        var resolvedJars = partitioned.resolvedJars();

        // Step 3: Extract task names from each JAR once, cache by coordinate
        Map<MavenCoordinate, Set<String>> taskNamesByCoordinate = new HashMap<>();
        for (var entry : resolvedJars.entrySet()) {
            ProgressManager.checkCanceled();

            var coord = entry.getKey();
            var jarPath = entry.getValue();

            if (!coord.isJar()) {
                continue;
            }

            indicator.setText2("Extracting from " + coord.getArtifactId());

            var taskNames = TASK_NAME_EXTRACTOR.extract(jarPath);
            if (!taskNames.isEmpty()) {
                taskNamesByCoordinate.put(coord, taskNames);
                LOG.debug("Found " + taskNames.size() + " tasks in " + coord.getArtifactId() + ": " + taskNames);
            }
        }

        // Step 4: Distribute task names to each scope based on its dependencies
        // Build a new map and swap atomically to remove stale entries
        Map<VirtualFile, Set<String>> newTaskNamesByScope = new HashMap<>();
        for (var scopeDep : scopeDependencies) {
            var root = scopeDep.root();

            Set<String> scopeTaskNames = new LinkedHashSet<>();
            for (var occ : scopeDep.occurrences()) {
                var tasks = taskNamesByCoordinate.get(occ.coordinate());
                if (tasks != null) {
                    scopeTaskNames.addAll(tasks);
                }
            }

            if (!scopeTaskNames.isEmpty()) {
                newTaskNamesByScope.put(root.getRootFile(), scopeTaskNames);
                LOG.debug("Scope " + root.getRootFile().getName() + ": " + scopeTaskNames.size() + " tasks");
            }
        }

        taskNamesByScope = Map.copyOf(newTaskNamesByScope);

        LOG.debug("Task loading complete. Scopes with tasks: " + taskNamesByScope.size());

        // Notify tracker about loaded dependencies
        notifyTracker(allCoordinates, modCountAtStart, isInitialLoad);

        reporter.finish(resolvedJars.size(), partitioned.errors().size());
    }

    private record PartitionResult(
            Map<MavenCoordinate, Path> resolvedJars,
            Map<MavenCoordinate, String> errors,
            Set<MavenCoordinate> skipped
    ) {}

    private static PartitionResult resolvePartitioned(@NotNull Set<MavenCoordinate> allCoordinates,
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
            return new PartitionResult(Map.of(), Map.of(), skipped);
        }

        ProgressManager.checkCanceled();

        indicator.setText("Resolving " + toResolve.size() + " artifacts...");
        reporter.reportResolving(toResolve.size());
        var resolveResult = resolver.resolveAll(toResolve);

        // Reclassify failed non-version-like coordinates as skipped instead of errors
        Map<MavenCoordinate, String> realErrors = new LinkedHashMap<>();
        for (var entry : resolveResult.errors().entrySet()) {
            if (entry.getKey().isResolvableVersion()) {
                realErrors.put(entry.getKey(), entry.getValue());
            } else {
                skipped.add(entry.getKey());
            }
        }

        return new PartitionResult(resolveResult.resolved(), realErrors, skipped);
    }

    private void restartInspections(@NotNull Set<VirtualFile> files) {
        if (project.isDisposed() || files.isEmpty()) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }
            var psiManager = PsiManager.getInstance(project);
            var analyzer = DaemonCodeAnalyzer.getInstance(project);
            for (var vf : files) {
                if (!vf.isValid()) {
                    continue;
                }
                var psiFile = psiManager.findFile(vf);
                if (psiFile != null) {
                    analyzer.restart(psiFile, "Concord dependency resolution changed");
                }
            }
        }, project.getDisposed());
    }

    private void updateProblemFiles(@NotNull Set<VirtualFile> newProblemFiles) {
        var old = markedProblemFiles;
        if (old.equals(newProblemFiles)) {
            return;
        }

        var wolf = WolfTheProblemSolver.getInstance(project);
        for (var file : old) {
            if (file.isValid() && !newProblemFiles.contains(file)) {
                wolf.clearProblemsFromExternalSource(file, DEPENDENCY_PROBLEM_SOURCE);
            }
        }
        for (var file : newProblemFiles) {
            if (file.isValid() && !old.contains(file)) {
                wolf.reportProblemsFromExternalSource(file, DEPENDENCY_PROBLEM_SOURCE);
            }
        }
        markedProblemFiles = newProblemFiles.isEmpty() ? Set.of() : Set.copyOf(newProblemFiles);
    }

    private void updateFailedAndRestart(@NotNull Map<MavenCoordinate, String> newErrors,
                                        @NotNull List<DependencyCollector.ScopeDependencies> scopeDependencies) {
        var newFailed = newErrors.isEmpty() ? Map.<MavenCoordinate, String>of() : Map.copyOf(newErrors);

        // Update problem file markers — must happen before the early return because
        // the set of affected files can change even when the error map stays the same
        Set<VirtualFile> newProblemFiles = new LinkedHashSet<>();
        for (var sd : scopeDependencies) {
            for (var occ : sd.occurrences()) {
                if (newFailed.containsKey(occ.coordinate())) {
                    newProblemFiles.add(occ.file());
                }
            }
        }
        // Capture before updateProblemFiles replaces the field
        var previousProblemFiles = markedProblemFiles;
        updateProblemFiles(newProblemFiles);

        if (failedDependencies.equals(newFailed)) {
            return;
        }
        failedDependencies = newFailed;

        // Targeted restart: current scope files + previously marked files (to clear stale annotations)
        Set<VirtualFile> files = new LinkedHashSet<>();
        for (var sd : scopeDependencies) {
            for (var occ : sd.occurrences()) {
                files.add(occ.file());
            }
        }
        files.addAll(previousProblemFiles);

        restartInspections(files);
    }

    private void notifyTracker(@NotNull Set<MavenCoordinate> loadedDeps,
                               long modCountAtStart,
                               boolean isInitialLoad) {
        if (project.isDisposed()) {
            return;
        }

        var tracker = DependencyChangeTracker.getInstance(project);
        if (isInitialLoad) {
            tracker.markInitialLoad(loadedDeps);
        } else {
            tracker.markReloaded(loadedDeps, modCountAtStart);
        }
    }

    @Override
    public void dispose() {
        taskNamesByScope = Map.of();
        failedDependencies = Map.of();
        skippedDependencies = Set.of();
        markedProblemFiles = Set.of();
    }
}
