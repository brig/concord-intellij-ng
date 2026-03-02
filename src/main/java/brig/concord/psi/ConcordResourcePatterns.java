// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

/**
 * Utilities for parsing and resolving resources.concord patterns.
 */
public final class ConcordResourcePatterns {

    private ConcordResourcePatterns() {
    }

    public static @NotNull String rootDirPrefix(@NotNull VirtualFile rootFile) {
        var parent = rootFile.getParent();
        var parentPath = parent != null ? parent.getPath() : "/";
        return parentPath.endsWith("/") ? parentPath : parentPath + "/";
    }

    public static @NotNull PathMatcher parsePattern(@NotNull String pattern, @NotNull String rootDirPrefix) {
        if (pattern.startsWith("glob:")) {
            var content = pattern.substring("glob:".length());
            var normalized = "glob:" + resolve(rootDirPrefix, content);
            return FileSystems.getDefault().getPathMatcher(normalized);
        }

        if (pattern.startsWith("regex:")) {
            var content = pattern.substring("regex:".length());
            var normalized = "regex:" + resolve(rootDirPrefix, content);
            return FileSystems.getDefault().getPathMatcher(normalized);
        }

        // Plain file path
        var singleFilePath = resolve(rootDirPrefix, pattern);
        var targetPath = Paths.get(singleFilePath).toAbsolutePath();
        return path -> path.toAbsolutePath().equals(targetPath);
    }

    private static @NotNull String resolve(@NotNull String baseDir, @NotNull String relative) {
        if (relative.startsWith("/")) {
            return baseDir + relative.substring(1);
        }
        return baseDir + relative;
    }
}
