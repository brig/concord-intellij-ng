// SPDX-License-Identifier: Apache-2.0
package brig.concord.run;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Console filter that creates hyperlinks for Concord CLI error messages.
 * Matches patterns like: (path/to/file.concord.yaml): Error @ line: 36, col: 7.
 */
public final class ConcordErrorFilter implements Filter {

    // Pattern: (file/path.yaml): Error @ line: 123, col: 45.
    private static final Pattern ERROR_PATTERN = Pattern.compile(
            "\\(([^)]+\\.(?:yml|yaml))\\):\\s*Error\\s*@\\s*line:\\s*(\\d+)(?:,\\s*col:\\s*(\\d+))?"
    );

    private final Project project;
    private final @Nullable String workingDirectory;

    public ConcordErrorFilter(@NotNull Project project, @Nullable String workingDirectory) {
        this.project = project;
        this.workingDirectory = workingDirectory;
    }

    @Override
    public @Nullable Result applyFilter(@NotNull String line, int entireLength) {
        var matcher = ERROR_PATTERN.matcher(line);
        if (!matcher.find()) {
            return null;
        }

        var filePath = matcher.group(1);
        var lineNum = Integer.parseInt(matcher.group(2));
        var colNum = matcher.group(3) != null ? Integer.parseInt(matcher.group(3)) : 1;

        // Calculate the offset within the entire console output
        var lineStart = entireLength - line.length();
        var highlightStart = lineStart + matcher.start(1) - 1; // -1 to include opening parenthesis
        var highlightEnd = lineStart + matcher.end(1) + 1;     // +1 to include closing parenthesis

        var hyperlinkInfo = new ConcordFileHyperlinkInfo(project, workingDirectory, filePath, lineNum, colNum);

        return new Result(highlightStart, highlightEnd, hyperlinkInfo);
    }

    private record ConcordFileHyperlinkInfo(
            @NotNull Project project,
            @Nullable String workingDirectory,
            @NotNull String filePath,
            int line,
            int column
    ) implements HyperlinkInfo {

        @Override
        public void navigate(@NotNull Project project) {
            var file = findFile();
            if (file == null) {
                return;
            }

            // Line and column in the error message are 1-based, IntelliJ uses 0-based
            var descriptor = new OpenFileDescriptor(project, file, line - 1, column - 1);
            FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
        }

        private @Nullable VirtualFile findFile() {
            // Try as absolute path first
            var file = LocalFileSystem.getInstance().findFileByPath(filePath);
            if (file != null) {
                return file;
            }

            // Try relative to working directory (from run configuration)
            if (workingDirectory != null) {
                var resolvedPath = Path.of(workingDirectory).resolve(filePath).toString();
                file = LocalFileSystem.getInstance().findFileByPath(resolvedPath);
                return file;
            }

            return null;
        }
    }
}
