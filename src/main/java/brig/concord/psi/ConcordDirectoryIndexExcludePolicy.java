// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import brig.concord.run.ConcordRunModeSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.impl.DirectoryIndexExcludePolicy;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

/**
 * Excludes the Concord CLI target directory from indexing.
 * <p>
 * When Concord CLI runs a flow, it copies project files into a target directory.
 * Without this policy, those copies appear in Search Everywhere, Find in Files,
 * and other IDE searches.
 * <p>
 * Reads the target directory path from {@link ConcordRunModeSettings} and resolves it
 * relative to the project base path. No VFS traversal or file index access needed.
 */
public final class ConcordDirectoryIndexExcludePolicy implements DirectoryIndexExcludePolicy {

    private final Project project;

    public ConcordDirectoryIndexExcludePolicy(@NotNull Project project) {
        this.project = project;
    }

    @Override
    public String @NotNull [] getExcludeUrlsForProject() {
        var targetDir = ConcordRunModeSettings.getInstance(project).getTargetDir();
        if (targetDir.isBlank()) {
            return ArrayUtil.EMPTY_STRING_ARRAY;
        }

        var targetPath = Path.of(targetDir);
        if (targetPath.isAbsolute()) {
            return new String[]{VfsUtilCore.pathToUrl(targetDir)};
        }

        var basePath = project.getBasePath();
        if (basePath == null) {
            return ArrayUtil.EMPTY_STRING_ARRAY;
        }

        return new String[]{VfsUtilCore.pathToUrl(Path.of(basePath).resolve(targetDir).toString())};
    }
}