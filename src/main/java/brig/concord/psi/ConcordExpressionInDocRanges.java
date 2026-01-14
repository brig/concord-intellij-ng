package brig.concord.psi;

import brig.concord.utils.ConcordExpressionRanges;
import brig.concord.yaml.psi.YAMLScalar;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public final class ConcordExpressionInDocRanges {

    public static @NotNull List<TextRange> findInScalarText(@NotNull Document doc, @NotNull YAMLScalar scalar) {
        var tr = scalar.getTextRange();
        var raw = doc.getText(tr);

        // Decide block vs non-block by first non-space char
        var first = firstNonSpace(raw);
        if (first >= 0) {
            var c = raw.charAt(first);
            if (c == '|' || c == '>') {
                return findInBlockScalar(doc, scalar);
            }
        }

        var local = ConcordExpressionRanges.find(raw);
        List<TextRange> out = new ArrayList<>(local.size());
        for (var r : local) {
            out.add(new TextRange(tr.getStartOffset() + r.getStartOffset(),
                    tr.getStartOffset() + r.getEndOffset()));
        }
        return out;
    }

    private static @NotNull List<TextRange> findInBlockScalar(@NotNull Document doc, @NotNull YAMLScalar scalar) {
        var tr = scalar.getTextRange();
        var raw = doc.getText(tr);

        var nl = raw.indexOf('\n');
        if (nl < 0) return List.of();

        var bodyStartInScalar = nl + 1;
        var bodyStartInDoc = tr.getStartOffset() + bodyStartInScalar;

        // split body lines
        var lines = splitLines(raw, bodyStartInScalar);

        var minIndent = Integer.MAX_VALUE;
        for (var ln : lines) {
            if (ln.text.isBlank()) continue;
            minIndent = Math.min(minIndent, countLeadingSpaces(ln.text));
        }
        if (minIndent == Integer.MAX_VALUE) minIndent = 0;

        var body = new StringBuilder();
        List<Integer> map = new ArrayList<>(raw.length()); // deindented index -> doc offset

        for (var li = 0; li < lines.size(); li++) {
            var ln = lines.get(li);
            var s = ln.text;

            var indent = Math.min(minIndent, countLeadingSpaces(s));
            var lineStartDoc = bodyStartInDoc + ln.startOffsetInBody;

            // append deindented line chars + mapping
            for (var k = indent; k < s.length(); k++) {
                body.append(s.charAt(k));
                map.add(lineStartDoc + k);
            }

            // append '\n' between lines (if it existed in raw)
            if (li < lines.size() - 1) {
                body.append('\n');
                // map newline to the doc's newline offset (end of line)
                map.add(lineStartDoc + s.length()); // points at '\n' in the document slice
            }
        }

        var deindentedBody = body.toString();
        var hits = ConcordExpressionRanges.find(deindentedBody);
        if (hits.isEmpty()) return List.of();

        List<TextRange> out = new ArrayList<>(hits.size());
        for (var r : hits) {
            int startDoc = map.get(r.getStartOffset());
            int endDocInclusive = map.get(r.getEndOffset() - 1);
            out.add(new TextRange(startDoc, endDocInclusive + 1));
        }
        return out;
    }

    private static int firstNonSpace(String s) {
        for (var i = 0; i < s.length(); i++) {
            if (!Character.isWhitespace(s.charAt(i))) return i;
        }
        return -1;
    }

    private static int countLeadingSpaces(String s) {
        var i = 0;
        while (i < s.length() && s.charAt(i) == ' ') i++;
        return i;
    }

    private static List<Line> splitLines(String raw, int start) {
        List<Line> out = new ArrayList<>();
        var i = start;
        var lineStart = start;

        while (i <= raw.length()) {
            if (i == raw.length() || raw.charAt(i) == '\n') {
                var line = raw.substring(lineStart, i);
                out.add(new Line(lineStart - start, line)); // offset in BODY
                i++;
                lineStart = i;
                continue;
            }
            i++;
        }
        return out;
    }

    private record Line(int startOffsetInBody, String text) {}

    private ConcordExpressionInDocRanges() {
    }
}
