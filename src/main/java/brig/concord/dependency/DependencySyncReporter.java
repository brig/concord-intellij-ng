package brig.concord.dependency;

import com.intellij.build.BuildDescriptor;
import com.intellij.build.DefaultBuildDescriptor;
import com.intellij.build.FilePosition;
import com.intellij.build.SyncViewManager;
import com.intellij.build.events.MessageEvent;
import com.intellij.build.progress.BuildProgress;
import com.intellij.build.progress.BuildProgressDescriptor;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Reports dependency resolution progress and errors to the Build tool window's Sync tab.
 */
@SuppressWarnings("UnstableApiUsage")
final class DependencySyncReporter {

    private static final String TITLE = "Concord Dependency Sync";

    private final @NotNull Project project;
    private @Nullable BuildProgress<BuildProgressDescriptor> buildProgress;

    DependencySyncReporter(@NotNull Project project) {
        this.project = project;
    }

    void start() {
        buildProgress = SyncViewManager.createBuildProgress(project);
        var descriptor = new DefaultBuildDescriptor(new Object(), TITLE, "", System.currentTimeMillis());
        descriptor.setActivateToolWindowWhenAdded(false);
        buildProgress.start(new BuildProgressDescriptor() {
            @Override
            public @NotNull String getTitle() {
                return TITLE;
            }

            @Override
            public @NotNull BuildDescriptor getBuildDescriptor() {
                return descriptor;
            }
        });
    }

    void reportCollecting() {
        if (buildProgress == null) return;

        buildProgress.output("Collecting dependencies...\n", true);
    }

    void reportResolving(int count) {
        if (buildProgress == null) return;

        buildProgress.output("Resolving " + count + " artifacts...\n", true);
    }

    void reportErrors(@NotNull Map<MavenCoordinate, String> errors,
                      @NotNull List<DependencyCollector.ScopeDependencies> scopeDeps) {
        if (buildProgress == null || errors.isEmpty()) return;

        for (var sd : scopeDeps) {
            for (var occ : sd.occurrences()) {
                var errorMsg = errors.get(occ.coordinate());
                if (errorMsg == null) continue;

                var filePosition = toFilePosition(occ);
                var message = "Cannot resolve " + occ.coordinate().toGav() + ": " + errorMsg;

                if (filePosition != null) {
                    buildProgress.fileMessage("Dependencies", message, MessageEvent.Kind.ERROR, filePosition);
                } else {
                    buildProgress.message("Dependencies", message, MessageEvent.Kind.ERROR, null);
                }
            }
        }
    }

    void finish(int resolvedCount, int errorCount) {
        if (buildProgress == null) return;

        var success = errorCount == 0;
        var message = success
                ? "Sync completed: " + resolvedCount + " dependencies resolved"
                : "Sync completed with errors: " + resolvedCount + " resolved, " + errorCount + " failed";

        if (success) {
            buildProgress.finish(System.currentTimeMillis(), false, message);
        } else {
            buildProgress.fail(System.currentTimeMillis(), message);
        }
    }

    private static @Nullable FilePosition toFilePosition(@NotNull DependencyCollector.DependencyOccurrence occ) {
        var vf = occ.file();
        var ioFile = VfsUtil.virtualToIoFile(vf);

        return ReadAction.compute(() -> {
            var doc = FileDocumentManager.getInstance().getDocument(vf);
            if (doc == null) {
                return new FilePosition(ioFile, 0, 0);
            }
            var line = doc.getLineNumber(occ.textOffset());
            var col = occ.textOffset() - doc.getLineStartOffset(line);
            return new FilePosition(ioFile, line, col);
        });
    }
}
