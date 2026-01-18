package brig.concord;

import brig.concord.yaml.psi.*;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public final class ConcordYamlPath {

    private final @NotNull YAMLFile file;

    // segment: name + optional [index], name may be absent for segments like "[0]"
    private static final Pattern SEGMENT =
            Pattern.compile("^(?<name>[^\\[]+)?(?<idx>\\[(?<n>\\d+)])?$");

    public ConcordYamlPath(@NotNull YAMLFile file) {
        this.file = file;
    }

    /** Safe: returns the PSI element of a key at path. */
    @NotNull
    public PsiElement keyElement(@NotNull String path) {
        return ReadAction.compute(() -> {
            var kv = resolve(path).key();
            var k = kv.getKey();
            if (k == null) {
                throw new AssertionError("Null key for path: " + path);
            }
            return k;
        });
    }

    /** Safe: returns start offset of the KEY scalar for the last key in the path. */
    public int keyStartOffset(@NotNull String path) {
        return ReadAction.compute(() -> {
            var kv = resolve(path).key();
            var k = kv.getKey();
            if (k == null) {
                throw new AssertionError("Null key for path: " + path);
            }
            return k.getTextRange().getStartOffset();
        });
    }

    /** Safe: returns value TextRange for the value addressed by the path. */
    public @NotNull TextRange valueRange(@NotNull String path) {
        return ReadAction.compute(() -> resolve(path).value().getTextRange());
    }

    /** Safe: returns the last key's text (normalized). */
    public @NotNull String keyText(@NotNull String path) {
        return ReadAction.compute(() -> resolve(path).key().getKeyText());
    }

    /** Safe: returns the PSI element of a value at path. */
    @NotNull
    public PsiElement valueElement(@NotNull String path) {
        return ReadAction.compute(() -> {
            var kv = resolve(path).key();
            var v = kv.getValue();
            if (v == null) {
                throw new AssertionError("Null value for path: " + path);
            }
            return v;
        });
    }

    private Resolution resolve(String rawPath) {
        var path = normalize(rawPath);

        var current = topLevelValue();
        if (current == null) {
            throw new AssertionError("Empty YAML document");
        }

        YAMLKeyValue lastKey = null;
        if (!path.isEmpty()) {
            for (var part : path.split("/")) {
                var s = parseSegment(part, path);

                // key access (mapping)
                if (s.name != null && !s.name.isEmpty()) {
                    var mapping = expectMapping(current, path);
                    var kv = findKey(mapping, s.name, path);
                    lastKey = kv;
                    current = expectNonNull(kv.getValue(), "Null value for key '" + s.name + "'", path);
                }

                // optional index access (sequence)
                if (s.index != null) {
                    var seq = expectSequence(current, path);
                    current = sequenceItem(seq, s.index, path);
                }
            }
        }

        if (lastKey == null) {
            throw new AssertionError("Path does not resolve to a key: " + rawPath);
        }

        return new Resolution(lastKey, current);
    }

    private @Nullable YAMLValue topLevelValue() {
        var docs = file.getDocuments();
        if (docs.isEmpty()) {
            return null;
        }
        return docs.getFirst().getTopLevelValue();
    }

    private static YAMLKeyValue findKey(YAMLMapping mapping, String key, String path) {
        for (var kv : mapping.getKeyValues()) {
            if (key.equals(kv.getKeyText())) {
                return kv;
            }
        }
        throw new AssertionError("Key '" + key + "' not found (path: " + path + ")");
    }

    private static YAMLValue sequenceItem(YAMLSequence seq, int index, String path) {
        var items = seq.getItems();
        if (index < 0 || index >= items.size()) {
            throw new AssertionError("Sequence index [" + index + "] out of bounds (" + items.size() + ") for path: " + path);
        }
        YAMLValue v = items.get(index).getValue();
        return expectNonNull(v, "Null sequence item value", path);
    }

    private static YAMLMapping expectMapping(YAMLValue v, String path) {
        if (v instanceof YAMLMapping) {
            return (YAMLMapping) v;
        }
        throw new AssertionError("Expected mapping, got " + type(v) + " (path: " + path + ")");
    }

    private static YAMLSequence expectSequence(YAMLValue v, String path) {
        if (v instanceof YAMLSequence) {
            return (YAMLSequence) v;
        }
        throw new AssertionError("Expected sequence, got " + type(v) + " (path: " + path + ")");
    }

    private static <T> T expectNonNull(T v, String msg, String path) {
        if (v != null) {
            return v;
        }
        throw new AssertionError(msg + " (path: " + path + ")");
    }

    private static Segment parseSegment(String raw, String fullPath) {
        var m = SEGMENT.matcher(raw);
        if (!m.matches()) {
            throw new AssertionError("Invalid path segment '" + raw + "' in path: " + fullPath);
        }
        var name = m.group("name");
        var n = m.group("n");
        var idx = n != null ? Integer.parseInt(n) : null;
        return new Segment(name, idx);
    }

    private static String normalize(String path) {
        var p = path.trim();
        while (p.startsWith("/")) {
            p = p.substring(1);
        }
        while (p.endsWith("/")) {
            p = p.substring(0, p.length() - 1);
        }
        return p;
    }

    private static String type(YAMLValue v) {
        return v == null ? "<null>" : v.getClass().getSimpleName();
    }

    private record Segment(@Nullable String name, @Nullable Integer index) {}
    private record Resolution(@NotNull YAMLKeyValue key, @NotNull YAMLValue value) {}
}

