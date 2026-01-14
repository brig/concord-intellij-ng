package brig.concord.utils;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class ConcordExpressionRanges {

    public static @NotNull List<TextRange> find(@Nullable String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        var out = new ArrayList<TextRange>();
        var n = text.length();
        var i = 0;

        while (i < n - 1) {
            var start = indexOfUnescaped(text, "${", i);
            if (start < 0) break;

            var j = start + 2; // right after ${
            var depth = 1;

            var inSingle = false;
            var inDouble = false;
            var escape = false;

            while (j < n) {
                var c = text.charAt(j);

                if (escape) {
                    escape = false;
                    j++;
                    continue;
                }

                if (c == '\\') {
                    escape = true;
                    j++;
                    continue;
                }

                // toggle quotes (only if not inside the other quote type)
                if (!inDouble && c == '\'') {
                    inSingle = !inSingle;
                    j++;
                    continue;
                }
                if (!inSingle && c == '"') {
                    inDouble = !inDouble;
                    j++;
                    continue;
                }

                // braces count only when not inside quotes
                if (!inSingle && !inDouble) {
                    if (c == '{') {
                        depth++;
                    } else if (c == '}') {
                        depth--;
                        if (depth == 0) {
                            out.add(new TextRange(start, j + 1)); // include closing }
                            j++;
                            break;
                        }
                    }
                }

                j++;
            }

            // unclosed ${...} -> stop
            if (j >= n) break;

            i = j;
        }

        return out;
    }

    /**
     * Like String.indexOf(needle, fromIndex), but:
     * - for needle "${" ensures '$' is NOT escaped by an odd number of backslashes.
     */
    private static int indexOfUnescaped(@NotNull String text, @NotNull String needle, int fromIndex) {
        var n = text.length();
        var needleLen = needle.length();

        var idx = fromIndex;
        while (idx <= n - needleLen) {
            var p = text.indexOf(needle, idx);
            if (p < 0) return -1;

            // For "${" check escaping of '$' at position p
            if (needle.equals("${")) {
                if (!isEscaped(text, p)) {
                    return p;
                }
                // escaped -> continue search after '$'
                idx = p + 1;
            } else {
                return p;
            }
        }
        return -1;
    }

    /**
     * Returns true if character at position 'pos' is escaped by an odd number of backslashes right before it.
     * Example: "\${" -> '$' escaped -> true
     *          "\\${" -> '$' not escaped -> false
     */
    private static boolean isEscaped(@NotNull String s, int pos) {
        var bs = 0;
        var i = pos - 1;
        while (i >= 0 && s.charAt(i) == '\\') {
            bs++;
            i--;
        }
        return (bs % 2) == 1;
    }

    private ConcordExpressionRanges() {
    }
}
