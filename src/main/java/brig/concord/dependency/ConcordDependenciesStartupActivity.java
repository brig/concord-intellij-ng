package brig.concord.dependency;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Startup activity that triggers initial load of Concord dependencies.
 * This establishes the baseline state for dependency change tracking.
 */
public final class ConcordDependenciesStartupActivity implements ProjectActivity {

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (project.isDisposed()) {
            return Unit.INSTANCE;
        }

        // Trigger initial load to establish baseline for change tracking
        TaskRegistry.getInstance(project).initialLoad();

        return Unit.INSTANCE;
    }
}
