// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import brig.concord.psi.ConcordScopeService;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.problems.WolfTheProblemSolver;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Project service that manages task names extracted from dependencies.
 * Coordinates loading, state management, and UI updates.
 */
@Service(Service.Level.PROJECT)
public final class TaskRegistry implements Disposable {

    private final Project project;
    private final AtomicBoolean reloading = new AtomicBoolean(false);
    private final AtomicReference<RegistryState> state = new AtomicReference<>(RegistryState.EMPTY);

    private static final Object DEPENDENCY_PROBLEM_SOURCE = new Object();

    public TaskRegistry(@NotNull Project project) {
        this.project = project;
    }

    public static @NotNull TaskRegistry getInstance(@NotNull Project project) {
        return project.getService(TaskRegistry.class);
    }

    public @NotNull Map<MavenCoordinate, String> getFailedDependencies() {
        return state.get().failedDependencies();
    }

    public @NotNull Set<MavenCoordinate> getSkippedDependencies() {
        return state.get().skippedDependencies();
    }

    public @NotNull Set<String> getTaskNames(@NotNull PsiElement context) {
        return getTaskInfos(context).keySet();
    }

    public @Nullable TaskInfo getTaskInfo(@NotNull PsiElement context, @NotNull String taskName) {
        return getTaskInfos(context).get(taskName);
    }

    public @NotNull Map<String, TaskInfo> getTaskInfos(@NotNull PsiElement context) {
        var psiFile = context.getContainingFile();
        if (psiFile == null) {
            return Map.of();
        }

        var virtualFile = psiFile.getOriginalFile().getVirtualFile();
        if (virtualFile == null) {
            return Map.of();
        }

        var scopeService = ConcordScopeService.getInstance(project);
        var scopes = scopeService.getScopesForFile(virtualFile);

        if (scopes.isEmpty()) {
            return Map.of();
        }

        var currentState = state.get();
        Map<String, TaskInfo> result = new LinkedHashMap<>();
        for (var scope : scopes) {
            result.putAll(currentState.getTasksForScope(scope.getRootFile()));
        }

        return result;
    }

    public void reload() {
        scheduleLoad(false);
    }

    public void initialLoad() {
        scheduleLoad(true);
    }

    private void scheduleLoad(boolean isInitialLoad) {
        if (!reloading.compareAndSet(false, true)) {
            return;
        }

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Loading Concord tasks", true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    var modCount = DependencyChangeTracker.getInstance(project).getCurrentModCount();
                    performLoad(indicator, modCount, isInitialLoad);
                } finally {
                    reloading.set(false);
                }
            }
        });
    }

    private void performLoad(@NotNull ProgressIndicator indicator, long modCountAtStart, boolean isInitialLoad) {
        if (project.isDisposed()) {
            return;
        }

        var reporter = new DependencySyncReporter(project);
        reporter.start();

        try {
            var loader = new TaskDependencyLoader(project);
            var newState = loader.load(indicator, reporter);

            applyNewState(newState);

            notifyTracker(newState, modCountAtStart, isInitialLoad);
        } catch (Exception e) {
            reporter.finish(0, 1);
            throw e;
        }
    }

    private void applyNewState(@NotNull RegistryState newState) {
        var oldState = state.getAndSet(newState);

        updateProblemFiles(oldState.problemFiles(), newState.problemFiles());

        if (!oldState.failedDependencies().equals(newState.failedDependencies()) ||
                !oldState.problemFiles().equals(newState.problemFiles())) {

            Set<VirtualFile> affectedFiles = new HashSet<>(newState.problemFiles());
            affectedFiles.addAll(oldState.problemFiles());
            restartInspections(affectedFiles);
        }
    }

    private void notifyTracker(RegistryState state, long modCount, boolean isInitial) {
        if (project.isDisposed()) {
            return;
        }

        Set<MavenCoordinate> allDeps = new HashSet<>();
        allDeps.addAll(state.resolvedDependencies());
        allDeps.addAll(state.failedDependencies().keySet());
        allDeps.addAll(state.skippedDependencies());

        var tracker = DependencyChangeTracker.getInstance(project);

        if (isInitial) {
            tracker.markInitialLoad(allDeps);
        } else {
            tracker.markReloaded(allDeps, modCount);
        }
    }

    private void updateProblemFiles(@NotNull Set<VirtualFile> oldFiles, @NotNull Set<VirtualFile> newFiles) {
        if (oldFiles.equals(newFiles)) {
            return;
        }

        var wolf = WolfTheProblemSolver.getInstance(project);

        for (var file : oldFiles) {
            if (file.isValid() && !newFiles.contains(file)) {
                wolf.clearProblemsFromExternalSource(file, DEPENDENCY_PROBLEM_SOURCE);
            }
        }

        for (var file : newFiles) {
            if (file.isValid() && !oldFiles.contains(file)) {
                wolf.reportProblemsFromExternalSource(file, DEPENDENCY_PROBLEM_SOURCE);
            }
        }
    }

    private void restartInspections(@NotNull Set<VirtualFile> files) {
        if (project.isDisposed() || files.isEmpty()) {
            return;
        }

        // Resolve PsiFiles on the background thread (slow operation) ...
        List<PsiFile> psiFiles = ReadAction.compute(() -> {
            if (project.isDisposed()) {
                return List.of();
            }
            var psiManager = PsiManager.getInstance(project);
            List<PsiFile> result = new ArrayList<>();
            for (var vf : files) {
                if (vf.isValid()) {
                    var psiFile = psiManager.findFile(vf);
                    if (psiFile != null) {
                        result.add(psiFile);
                    }
                }
            }
            return result;
        });

        // ... then restart inspections on EDT
        ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) {
                return;
            }
            var analyzer = DaemonCodeAnalyzer.getInstance(project);
            for (var psiFile : psiFiles) {
                if (psiFile.isValid()) {
                    analyzer.restart(psiFile, "Concord dependency resolution changed");
                }
            }
        }, project.getDisposed());
    }

    @TestOnly
    public void setFailedDependencies(@NotNull Map<MavenCoordinate, String> failures) {
        var current = state.get();
        state.set(new RegistryState(
                current.taskInfosByScope(),
                Map.copyOf(failures),
                current.skippedDependencies(),
                current.resolvedDependencies(),
                current.problemFiles()
        ));
    }

    @TestOnly
    public void setSkippedDependencies(@NotNull Set<MavenCoordinate> skipped) {
        var current = state.get();
        state.set(new RegistryState(
                current.taskInfosByScope(),
                current.failedDependencies(),
                Set.copyOf(skipped),
                current.resolvedDependencies(),
                current.problemFiles()
        ));
    }

    @TestOnly
    public void setTaskInfos(@NotNull VirtualFile scopeRoot, @NotNull Map<String, TaskInfo> taskInfos) {
        var current = state.get();
        Map<VirtualFile, Map<String, TaskInfo>> newTasks = new HashMap<>(current.taskInfosByScope());
        newTasks.put(scopeRoot, taskInfos);

        state.set(new RegistryState(
                newTasks,
                current.failedDependencies(),
                current.skippedDependencies(),
                current.resolvedDependencies(),
                current.problemFiles()
        ));
    }

    @TestOnly
    public void setTaskNames(@NotNull VirtualFile scopeRoot, @NotNull Set<String> taskNames) {
        Map<String, TaskInfo> infos = new LinkedHashMap<>();
        for (var name : taskNames) {
            infos.put(name, new TaskInfo(name, List.of()));
        }
        setTaskInfos(scopeRoot, infos);
    }

    @Override
    public void dispose() {
        state.set(RegistryState.EMPTY);
    }
}
