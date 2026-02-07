package brig.concord.dependency;

import brig.concord.psi.ConcordScopeService;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Project service that manages task names extracted from dependencies.
 * Provides task names for autocomplete in the task: field.
 */
@Service(Service.Level.PROJECT)
public final class TaskRegistry {

    private static final Logger LOG = Logger.getInstance(TaskRegistry.class);
    private static final TaskNameExtractor TASK_NAME_EXTRACTOR = new TaskNameExtractor();

    private final Project project;
    private final AtomicBoolean reloading = new AtomicBoolean(false);

    // Task names grouped by scope (root file). Swapped atomically to avoid read-during-update races.
    private volatile Map<VirtualFile, Set<String>> taskNamesByScope = Map.of();

    public TaskRegistry(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull TaskRegistry getInstance(@NotNull Project project) {
        return project.getService(TaskRegistry.class);
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
     * @param indicator progress indicator
     * @param modCountAtStart modification count at start (ignored for initial load)
     * @param isInitialLoad true if this is initial load (no prior state)
     */
    private void loadTaskNames(@NotNull ProgressIndicator indicator,
                               long modCountAtStart,
                               boolean isInitialLoad) {
        if (project.isDisposed()) {
            return;
        }

        LOG.info("Loading task names from dependencies (initial=" + isInitialLoad + ")...");

        var collector = DependencyCollector.getInstance(project);
        var resolver = new DependencyResolver(project);

        indicator.setText("Collecting dependencies...");
        var scopeDependencies = ReadAction.compute(() -> {
            if (project.isDisposed()) {
                return List.<DependencyCollector.ScopeDependencies>of();
            }
            return collector.collectByScope();
        });

        if (scopeDependencies.isEmpty()) {
            LOG.info("No scopes found");
            notifyTracker(Set.of(), modCountAtStart, isInitialLoad);
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
            LOG.info("No dependencies found");
            notifyTracker(Set.of(), modCountAtStart, isInitialLoad);
            return;
        }

        LOG.info("Found " + allCoordinates.size() + " unique dependencies across " + scopeDependencies.size() + " scopes");

        // Step 2: Resolve all coordinates at once
        indicator.setText("Resolving " + allCoordinates.size() + " artifacts...");
        ProgressManager.checkCanceled();
        var resolvedJars = resolver.resolveAll(allCoordinates);

        // Step 3: Extract task names from each JAR once, cache by coordinate
        Map<MavenCoordinate, Set<String>> taskNamesByCoordinate = new HashMap<>();
        for (var entry : resolvedJars.entrySet()) {
            ProgressManager.checkCanceled();

            var coord = entry.getKey();
            var jarPath = entry.getValue();

            indicator.setText2("Extracting from " + coord.getArtifactId());

            var taskNames = TASK_NAME_EXTRACTOR.extract(jarPath);
            if (!taskNames.isEmpty()) {
                taskNamesByCoordinate.put(coord, taskNames);
                LOG.info("Found " + taskNames.size() + " tasks in " + coord.getArtifactId() + ": " + taskNames);
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
                LOG.info("Scope " + root.getRootFile().getName() + ": " + scopeTaskNames.size() + " tasks");
            }
        }

        taskNamesByScope = Map.copyOf(newTaskNamesByScope);

        LOG.info("Task loading complete. Scopes with tasks: " + taskNamesByScope.size());

        // Notify tracker about loaded dependencies
        notifyTracker(allCoordinates, modCountAtStart, isInitialLoad);
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
}
