package brig.concord.psi;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import brig.concord.yaml.psi.YAMLDocument;
import brig.concord.yaml.psi.YAMLScalar;
import brig.concord.yaml.psi.YAMLSequence;
import brig.concord.yaml.psi.YAMLSequenceItem;

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

    private final VirtualFile rootFile;
    private final Path rootDir;
    private final List<PathMatcher> patterns;

    /**
     * Creates a ConcordRoot from a root YAML document.
     *
     * @param rootFile the root concord.yaml file
     * @param rootDoc  the parsed YAML document (may be null if file couldn't be parsed)
     */
    public ConcordRoot(@NotNull VirtualFile rootFile, @Nullable YAMLDocument rootDoc) {
        this.rootFile = rootFile;

        var parent = rootFile.getParent();
        this.rootDir = parent != null ? Paths.get(parent.getPath()) : Paths.get("/");

        this.patterns = parsePatterns(rootDoc);
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
        return rootDir.getFileName() != null ? rootDir.getFileName().toString() : rootFile.getName();
    }

    /**
     * Returns the patterns for matching files in this scope.
     */
    public @NotNull List<PathMatcher> getPatterns() {
        return patterns;
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

        var filePath = Paths.get(file.getPath());
        for (var pattern : patterns) {
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

        // Extract pattern strings from PSI within read action
        var patternStrings = ReadAction.compute(() -> {
            var resources = YamlPsiUtils.get(rootDoc, YAMLSequence.class, "resources", "concord");
            if (resources == null) {
                return Collections.<String>emptyList();
            }

            List<String> strings = new ArrayList<>();
            for (var item : resources.getItems()) {
                if (item.getValue() instanceof YAMLScalar scalar) {
                    var pattern = scalar.getTextValue();
                    if (!pattern.isBlank()) {
                        strings.add(pattern.trim());
                    }
                }
            }
            return strings;
        });

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
        var baseDir = rootDir.toString();

        if (pattern.startsWith("glob:")) {
            var normalized = "glob:" + concat(baseDir, pattern.substring("glob:".length()));
            return FileSystems.getDefault().getPathMatcher(normalized);
        }

        if (pattern.startsWith("regex:")) {
            var normalized = "regex:" + concat(baseDir, pattern.substring("regex:".length()));
            return FileSystems.getDefault().getPathMatcher(normalized);
        }

        // Plain file path
        var singleFilePath = concat(baseDir, pattern);
        var targetPath = Paths.get(singleFilePath).toAbsolutePath();
        return path -> path.toAbsolutePath().equals(targetPath);
    }

    private static @NotNull String concat(@NotNull String path, @NotNull String str) {
        var separator = "/";
        if (str.startsWith("/")) {
            separator = "";
        }
        return path + separator + str;
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
                ", patterns=" + patterns.size() +
                '}';
    }
}
