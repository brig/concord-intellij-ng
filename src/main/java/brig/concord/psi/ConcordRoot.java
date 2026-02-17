// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.psi;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLSequence;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Concord project root defined by a root concord.yaml file.
 * A root file defines which additional Concord files belong to its scope
 * via the resources.concord configuration.
 */
public final class ConcordRoot {

    private static final String DEFAULT_CONCORD_RESOURCES = "glob:concord/{**/,}{*.,}concord.{yml,yaml}";
    private static final Key<CachedValue<List<PathMatcher>>> PATTERNS_CACHE_KEY =
            Key.create("ConcordRoot.patterns");

    private final Project project;
    private final VirtualFile rootFile;
    private final Path rootDir;
    private final String rootDirPrefix;

    /**
     * Creates a ConcordRoot from a root YAML document.
     *
     * @param project  the project
     * @param rootFile the root concord.yaml file
     */
    public ConcordRoot(@NotNull Project project, @NotNull VirtualFile rootFile) {
        this.project = project;
        this.rootFile = rootFile;

        var parent = rootFile.getParent();
        var parentPath = parent != null ? parent.getPath() : "/";

        this.rootDir = Paths.get(parentPath);
        this.rootDirPrefix = parentPath.endsWith("/") ? parentPath : parentPath + "/";
    }

    /**
     * Returns the root concord.yaml file.
     */
    public @NotNull VirtualFile getRootFile() {
        return rootFile;
    }

    /**
     * Returns the directory containing the root file.
     */
    public @NotNull Path getRootDir() {
        return rootDir;
    }

    /**
     * Returns the scope name, which is the name of the directory containing the root file.
     */
    public @NotNull String getScopeName() {
        return rootDir.getFileName().toString();
    }

    public @NotNull List<PathMatcher> getPatterns() {
        var psiFile = PsiManager.getInstance(project).findFile(rootFile);
        if (psiFile == null) {
            return Collections.singletonList(parsePattern(DEFAULT_CONCORD_RESOURCES));
        }

        // Cache is stored on PsiFile, survives ConcordRoot recreation
        return CachedValuesManager.getCachedValue(psiFile, PATTERNS_CACHE_KEY, () -> {
            var doc = PsiTreeUtil.getChildOfType(psiFile, YAMLDocument.class);
            var patterns = parsePatterns(doc);
            return CachedValueProvider.Result.create(patterns, psiFile);
        });
    }

    /**
     * Checks if the given file belongs to this scope.
     * A file belongs if it's the root file itself or matches any of the patterns.
     *
     * @param file the file to check
     * @return true if the file belongs to this scope
     */
    public boolean contains(@NotNull VirtualFile file) {
        if (file.equals(rootFile)) {
            return true;
        }

        // if file is not under root directory, it can't match any pattern
        // (all patterns are prefixed with rootDir in parsePattern)
        String filePathStr = file.getPath();
        if (!filePathStr.startsWith(rootDirPrefix)) {
            return false;
        }

        var filePath = Paths.get(filePathStr);
        for (var pattern : getPatterns()) {
            if (pattern.matches(filePath)) {
                return true;
            }
        }
        return false;
    }

    private @NotNull List<PathMatcher> parsePatterns(@Nullable YAMLDocument rootDoc) {
        if (rootDoc == null) {
            return Collections.singletonList(parsePattern(DEFAULT_CONCORD_RESOURCES));
        }

        var resources = YamlPsiUtils.get(rootDoc, YAMLSequence.class, "resources", "concord");
        if (resources == null) {
            return Collections.singletonList(parsePattern(DEFAULT_CONCORD_RESOURCES));
        }

        List<String> patternStrings = new ArrayList<>();
        for (var item : resources.getItems()) {
            if (item.getValue() instanceof YAMLScalar scalar) {
                var pattern = scalar.getTextValue();
                if (!pattern.isBlank()) {
                    patternStrings.add(pattern.trim());
                }
            }
        }

        if (patternStrings.isEmpty()) {
            return Collections.singletonList(parsePattern(DEFAULT_CONCORD_RESOURCES));
        }

        List<PathMatcher> result = new ArrayList<>();
        for (var patternString : patternStrings) {
            result.add(parsePattern(patternString));
        }

        return Collections.unmodifiableList(result);
    }

    private @NotNull PathMatcher parsePattern(@NotNull String pattern) {
        var baseDir = rootDirPrefix; // Always ends with "/" and uses forward slashes

        if (pattern.startsWith("glob:")) {
            var content = pattern.substring("glob:".length());
            var normalized = "glob:" + resolve(baseDir, content);
            return FileSystems.getDefault().getPathMatcher(normalized);
        }

        if (pattern.startsWith("regex:")) {
            var content = pattern.substring("regex:".length());
            var normalized = "regex:" + resolve(baseDir, content);
            return FileSystems.getDefault().getPathMatcher(normalized);
        }

        // Plain file path
        var singleFilePath = resolve(baseDir, pattern);
        var targetPath = Paths.get(singleFilePath).toAbsolutePath();
        return path -> path.toAbsolutePath().equals(targetPath);
    }

    private static @NotNull String resolve(@NotNull String baseDir, @NotNull String relative) {
        // baseDir is guaranteed to end with '/'
        if (relative.startsWith("/")) {
            return baseDir + relative.substring(1);
        }
        return baseDir + relative;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (ConcordRoot) o;
        return rootFile.equals(that.rootFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootFile);
    }

    @Override
    public String toString() {
        return "ConcordRoot{" +
                "rootFile=" + rootFile.getPath() +
                '}';
    }
}
