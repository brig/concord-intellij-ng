// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.dependency;

import brig.concord.psi.ConcordDependenciesListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ide.ActivityTracker;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tracks changes to Concord dependencies and determines when reload is needed.
 * <p>
 * Compares actual dependency content (not just modification count) to avoid
 * false positives from whitespace-only changes.
 * <p>
 * Thread-safe. All mutable state is guarded by {@code synchronized(this)}.
 */
@Service(Service.Level.PROJECT)
public final class DependencyChangeTracker implements Disposable {

    private static final Logger LOG = Logger.getInstance(DependencyChangeTracker.class);

    private static final int CHECK_DELAY_MS = 300;

    private final Project project;
    private final AtomicReference<ScheduledFuture<?>> pendingCheck = new AtomicReference<>();

    // --- All fields below are guarded by synchronized(this) ---

    /**
     * Dependencies loaded during last successful reload.
     * null means never loaded (initial state).
     */
    private Set<MavenCoordinate> lastLoadedDeps = null;

    /**
     * True if dependencies have changed and reload is needed.
     */
    private boolean dirty = false;

    /**
     * True if user dismissed the notification.
     * Reset when dependencies change again.
     */
    private boolean dismissed = false;

    /**
     * Modification count when user dismissed.
     * Used to detect if deps changed since dismissal.
     */
    private long dismissedModCount = -1;

    public DependencyChangeTracker(@NotNull Project project) {
        this.project = project;

        project.getMessageBus().connect(this)
                .subscribe(ConcordDependenciesListener.TOPIC,
                        (ConcordDependenciesListener) this::scheduleCheck);
    }

    public static @NotNull DependencyChangeTracker getInstance(@NotNull Project project) {
        return project.getService(DependencyChangeTracker.class);
    }

    /**
     * Schedules a check for dependency changes.
     * Called by {@link brig.concord.psi.ConcordModificationTracker} when
     * dependencies section is modified.
     * <p>
     * Uses debouncing to avoid checking on every keystroke.
     * Skips check if already dirty (optimization).
     */
    public void scheduleCheck() {
        if (project.isDisposed()) {
            return;
        }

        synchronized (this) {
            // Already dirty - no need to check again
            if (dirty) {
                return;
            }

            // Never loaded yet - nothing to compare against
            if (lastLoadedDeps == null) {
                return;
            }
        }

        var prev = pendingCheck.getAndSet(
                AppExecutorUtil.getAppScheduledExecutorService()
                        .schedule(this::checkForChanges, CHECK_DELAY_MS, TimeUnit.MILLISECONDS)
        );
        if (prev != null) {
            prev.cancel(false);
        }
    }

    /**
     * Returns true if dependencies have changed and reload is needed.
     * Takes dismissal state into account.
     * <p>
     * This method is read-only — it does not mutate state.
     */
    public boolean needsReload() {
        long currentModCount = getCurrentModCount();

        synchronized (this) {
            if (!dirty) {
                return false;
            }

            return !dismissed || currentModCount != dismissedModCount;
        }
    }

    /**
     * Marks dependencies as successfully reloaded.
     * Called by {@link TaskRegistry} after successful reload.
     *
     * @param loadedDeps the dependencies that were loaded
     * @param modCountAtStart modification count at reload start
     */
    public void markReloaded(@NotNull Set<MavenCoordinate> loadedDeps, long modCountAtStart) {
        if (project.isDisposed()) {
            return;
        }

        long currentModCount = getCurrentModCount();

        synchronized (this) {
            // Check if dependencies changed during reload
            if (currentModCount != modCountAtStart) {
                // Don't update lastLoadedDeps - they might be stale
                // Schedule another check (outside sync block)
            } else {
                lastLoadedDeps = Set.copyOf(loadedDeps);
                dirty = false;
                dismissed = false;
            }
        }

        if (currentModCount != modCountAtStart) {
            scheduleCheck();
        } else {
            updateNotifications();
        }
    }

    /**
     * Called for initial load when no previous state exists.
     * Sets baseline without clearing dirty flag (there's nothing to clear).
     */
    public void markInitialLoad(@NotNull Set<MavenCoordinate> loadedDeps) {
        if (project.isDisposed()) {
            return;
        }

        synchronized (this) {
            lastLoadedDeps = Set.copyOf(loadedDeps);
            // Don't touch dirty flag - it should be false anyway
        }
    }

    /**
     * Dismisses the reload notification until dependencies change again.
     */
    public void dismiss() {
        synchronized (this) {
            dismissed = true;
            dismissedModCount = getCurrentModCount();
        }

        updateNotifications();
    }

    /**
     * Returns the current modification count for dependencies.
     */
    public long getCurrentModCount() {
        return brig.concord.psi.ConcordModificationTracker.getInstance(project)
                .dependencies()
                .getModificationCount();
    }

    /**
     * For testing: run the change check synchronously instead of via scheduled executor.
     */
    @TestOnly
    public void checkForChangesNow() {
        checkForChanges();
    }

    /**
     * For testing: directly set the dirty state and last loaded deps.
     */
    @TestOnly
    public void setStateForTest(Set<MavenCoordinate> deps, boolean isDirty) {
        synchronized (this) {
            lastLoadedDeps = deps != null ? Set.copyOf(deps) : null;
            dirty = isDirty;
            dismissed = false;
        }
    }

    /**
     * Performs the actual check for dependency changes.
     * Runs in a pooled thread via scheduled executor.
     */
    private void checkForChanges() {
        if (project.isDisposed()) {
            return;
        }

        synchronized (this) {
            // Double-check dirty flag (might have changed since check was scheduled)
            if (dirty) {
                return;
            }
        }

        Set<MavenCoordinate> currentDeps;
        try {
            currentDeps = ReadAction.compute(() -> {
                if (project.isDisposed()) {
                    return null;
                }
                return DependencyCollector.getInstance(project).collectAll();
            });
        } catch (Exception e) {
            LOG.debug("Failed to collect dependencies for change check", e);
            return;
        }

        if (currentDeps == null) {
            return;
        }

        boolean changed;
        synchronized (this) {
            Set<MavenCoordinate> baseline = lastLoadedDeps;

            if (baseline == null) {
                // Never loaded - shouldn't happen if scheduleCheck guards properly
                return;
            }

            changed = !currentDeps.equals(baseline);
            if (changed) {
                dirty = true;
                dismissed = false;
            }
        }

        if (changed) {
            updateNotifications();
        }
    }

    /**
     * Triggers UI update to reflect current state.
     * Uses ActivityTracker to notify the platform that action presentations
     * should be re-evaluated (which updates floating toolbars).
     */
    private void updateNotifications() {
        if (project.isDisposed()) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(
                () -> ActivityTracker.getInstance().inc(),
                project.getDisposed()
        );
    }

    @Override
    public void dispose() {
        var future = pendingCheck.getAndSet(null);
        if (future != null) {
            future.cancel(false);
        }
    }
}
