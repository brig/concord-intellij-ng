// SPDX-License-Identifier: Apache-2.0
package brig.concord.highlighting;

import brig.concord.ConcordFileType;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory;
import com.intellij.openapi.util.TextRange;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import java.util.*;
import java.util.stream.Collectors;

import static brig.concord.ConcordYamlTestBaseJunit5.*;

public class HighlightAssertion {

    private final CodeInsightTestFixture myFixture;
    private final AbstractTarget target;
    private List<HighlightInfo> cachedInfos;
    private SyntaxHighlighter cachedSyntaxHighlighter;

    public HighlightAssertion(CodeInsightTestFixture myFixture, AbstractTarget target) {
        this.myFixture = myFixture;
        this.target = target;
    }

    /** Assert target is highlighted with expected key */
    public void is(@NotNull TextAttributesKey expected) {
        assertHighlightAtRange(target.text(), target.range(), expected);
    }

    /** Assert target is NOT highlighted with forbidden key */
    public void isNot(@NotNull TextAttributesKey forbidden) {
        assertNotHighlightAtRange(target.text(), target.range(), forbidden);
    }

    /** Assert target is highlighted ONLY with the expected key (no other highlights apply) */
    public HighlightAssertion isOnly(TextAttributesKey expected) {
        return isOnly(expected, new TextAttributesKey[0]);
    }

    /** Assert target is highlighted only with expected key, allowing specified extra keys */
    public HighlightAssertion isOnly(@NotNull TextAttributesKey expected,
                                     @NotNull TextAttributesKey... allowedExtras) {
        var range = target.range();
        int start = range.getStartOffset();
        int end = range.getEndOffset();

        var allowed = new LinkedHashSet<TextAttributesKey>();
        allowed.add(expected);
        Collections.addAll(allowed, allowedExtras);

        var all = new LinkedHashSet<TextAttributesKey>();
        all.addAll(getLexerHighlightAt(start));
        all.addAll(daemonKeysCovering(start, end));

        if (!all.contains(expected)) {
            Assertions.fail("Expected highlight '" + expected.getExternalName() + "' not found. Keys: " + describeKeys(all));
        }

        var forbidden = all.stream().filter(k -> !allowed.contains(k)).toList();
        if (!forbidden.isEmpty()) {
            Assertions.fail(
                    "Found forbidden highlights: " + describeKeys(forbidden) + "\n" +
                            "Allowed: " + describeKeys(allowed) + "\n" +
                            "All: " + describeKeys(all) + "\n" +
                            "Range: " + start + ".." + end + "\n" +
                            "Target text:\n" + target.text()
            );
        }

        return this;
    }

    /** Assert that at least one highlight with expected key INTERSECTS target range */
    public HighlightAssertion contains(@NotNull TextAttributesKey expected) {
        assertHighlightIntersectsRange(target.range(), expected);
        return this;
    }

    /** Assert that each expected key has at least one highlight intersecting target range */
    public HighlightAssertion containsAll(@NotNull TextAttributesKey... expectedKeys) {
        for (var k : expectedKeys) {
            assertHighlightIntersectsRange(target.range(), k);
        }
        return this;
    }

    /**
     * Assert that at least one highlight with expected key intersects target range,
     * and the highlighted text (intersection) matches expectedText (whitespace-normalized).
     */
    public HighlightAssertion contains(@NotNull TextAttributesKey expected,
                                       @NotNull String expectedHighlightedText) {
        assertHighlightIntersectsRangeWithText(target.range(), expected, expectedHighlightedText);
        return this;
    }

    public HighlightAssertion containsExactly(@NotNull TextAttributesKey expected, int expectedCount) {
        var actual = countMergedSlices(expected);
        if (actual != expectedCount) {
            Assertions.fail(
                    "Expected exactly " + expectedCount + " occurrences of '" + expected.getExternalName() + "' " +
                            "inside range " + target.range().getStartOffset() + ".." + target.range().getEndOffset() + "\n" +
                            "Target text:\n" + target.text() + "\n\n" +
                            "Actual merged slices (" + actual + "):\n  " +
                            String.join("\n  ", describeMergedSlices(expected))
            );
        }
        return this;
    }

    public HighlightAssertion notContains(@NotNull TextAttributesKey forbidden) {
        var hostRange = target.range();
        var infos = infos();

        var hits = new ArrayList<String>();

        for (var info : infos) {
            var key = infoKey(info);
            if (!forbidden.equals(key)) {
                continue;
            }

            var r = new TextRange(info.getStartOffset(), info.getEndOffset());
            var inter = r.intersection(hostRange);
            if (inter == null || inter.isEmpty()) {
                continue;
            }

            hits.add(formatHit(info, key, inter));
        }

        Assertions.assertTrue(
                hits.isEmpty(),
                "Did NOT expect highlight '" + forbidden.getExternalName() + "' intersecting range " +
                        hostRange.getStartOffset() + ".." + hostRange.getEndOffset() + "\n" +
                        "Target text:\n" + target.text() + "\n\n" +
                        "Forbidden hits:\n  " + (hits.isEmpty() ? "<none>" : String.join("\n  ", hits))
        );

        return this;
    }

    private List<HighlightInfo> infos() {
        if (cachedInfos == null) {
            cachedInfos = myFixture.doHighlighting();
        }
        return cachedInfos;
    }

    protected void assertHighlightAtRange(@NotNull String text, @NotNull TextRange range,
                                          @NotNull TextAttributesKey expected) {
        var offset = range.getStartOffset();
        var endOffset = range.getEndOffset();

        // Lexer highlighting
        var lexerKeys = getLexerHighlightAt(offset);
        var foundInLexer = lexerKeys.contains(expected);

        // Daemon highlighting
        var infos = infos();

        List<String> visitorHits = new ArrayList<>();
        var foundInVisitor = false;

        for (var info : infos) {
            if (info.getStartOffset() <= offset && info.getEndOffset() >= endOffset) {
                var key = infoKey(info);

                visitorHits.add(String.format(
                        "[%d..%d] key=%s forced=%s",
                        info.getStartOffset(),
                        info.getEndOffset(),
                        key != null ? key.getExternalName() : "<null>",
                        info.forcedTextAttributesKey != null
                ));

                if (expected.equals(key)) {
                    foundInVisitor = true;
                }
            }
        }

        Assertions.assertTrue(
                foundInLexer || foundInVisitor,
                "Expected highlight '" + expected.getExternalName() + "' for text '" + text + "'\n" +
                        "Offset: " + offset + ".." + endOffset + "\n\n" +
                        "Lexer keys at offset:\n  " +
                        (lexerKeys.isEmpty()
                                ? "<none>"
                                : lexerKeys.stream()
                                .map(TextAttributesKey::getExternalName)
                                .collect(Collectors.joining(", "))) +
                        "\n\nVisitor highlights covering range:\n  " +
                        (visitorHits.isEmpty()
                                ? "<none>"
                                : String.join("\n  ", visitorHits))
        );
    }

    protected void assertNotHighlightAtRange(@NotNull String text, @NotNull TextRange range,
                                             @NotNull TextAttributesKey forbidden) {
        var offset = range.getStartOffset();
        var endOffset = range.getEndOffset();

        var lexerKeys = getLexerHighlightAt(offset);
        var foundInLexer = lexerKeys.contains(forbidden);

        var infos = infos();
        var foundInVisitor = false;
        var visitorHits = new ArrayList<String>();

        for (var info : infos) {
            if (info.getStartOffset() <= offset && info.getEndOffset() >= endOffset) {
                var key = infoKey(info);

                visitorHits.add(String.format(
                        "[%d..%d] key=%s forced=%s",
                        info.getStartOffset(),
                        info.getEndOffset(),
                        key != null ? key.getExternalName() : "<null>",
                        info.forcedTextAttributesKey != null
                ));

                if (forbidden.equals(key)) {
                    foundInVisitor = true;
                }
            }
        }

        Assertions.assertFalse(
                foundInLexer || foundInVisitor,
                "Did NOT expect highlight '" + forbidden.getExternalName() + "' for text '" + text + "'\n" +
                        "Offset: " + offset + ".." + endOffset + "\n\n" +
                        "Lexer keys:\n  " +
                        (lexerKeys.isEmpty()
                                ? "<none>"
                                : lexerKeys.stream().map(TextAttributesKey::getExternalName)
                                .collect(Collectors.joining(", "))) +
                        "\n\nVisitor highlights:\n  " +
                        (visitorHits.isEmpty() ? "<none>" : String.join("\n  ", visitorHits))
        );
    }

    @NotNull
    protected List<TextAttributesKey> getLexerHighlightAt(int offset) {
        var editor = myFixture.getEditor();
        var doc = editor.getDocument();
        int len = doc.getTextLength();
        if (len == 0) {
            return List.of();
        }
        offset = Math.min(Math.max(offset, 0), len - 1);

        var highlighter = editor.getHighlighter();
        var it = highlighter.createIterator(offset);

        List<TextAttributesKey> result = new ArrayList<>();
        if (!it.atEnd() && it.getStart() <= offset && it.getEnd() > offset) {
            var tokenType = it.getTokenType();
            var syntaxHighlighter = syntaxHighlighter();
            if (syntaxHighlighter != null) {
                var keys = syntaxHighlighter.getTokenHighlights(tokenType);
                Collections.addAll(result, keys);
            }
        }
        return result;
    }

    private void assertHighlightIntersectsRange(@NotNull TextRange hostRange,
                                                @NotNull TextAttributesKey expected) {
        var infos = infos();
        var hits = new ArrayList<String>();

        var found = false;
        for (var info : infos) {
            var key = infoKey(info);
            if (!expected.equals(key)) {
                continue;
            }

            var r = new TextRange(info.getStartOffset(), info.getEndOffset());
            var inter = r.intersection(hostRange);
            if (inter == null || inter.isEmpty()) {
                continue;
            }

            hits.add(formatHit(info, key, inter));
            found = true;
        }

        Assertions.assertTrue(
                found,
                "Expected at least one highlight '" + expected.getExternalName() + "' intersecting range " +
                        hostRange.getStartOffset() + ".." + hostRange.getEndOffset() + "\n" +
                        "Target text:\n" + target.text() + "\n\n" +
                        "Matching hits:\n  " + (hits.isEmpty() ? "<none>" : String.join("\n  ", hits)) + "\n\n" +
                        "All highlights intersecting range:\n  " +
                        String.join("\n  ", collectAllIntersectingHits(hostRange))
        );
    }

    private void assertHighlightIntersectsRangeWithText(@NotNull TextRange hostRange,
                                                        @NotNull TextAttributesKey expected,
                                                        @NotNull String expectedHighlightedText) {
        var doc = myFixture.getEditor().getDocument();
        var infos = infos();

        var expectedNorm = normalizeWs(expectedHighlightedText);

        var candidates = new ArrayList<String>();
        for (var info : infos) {
            var key = infoKey(info);
            if (!expected.equals(key)) {
                continue;
            }

            var r = new TextRange(info.getStartOffset(), info.getEndOffset());
            var inter = r.intersection(hostRange);
            if (inter == null || inter.isEmpty()) {
                continue;
            }

            var actualText = doc.getText(inter);
            var actualNorm = normalizeWs(actualText);

            candidates.add(
                    formatHit(info, key, inter) +
                            "\n    text: " + preview(actualText)
            );

            if (expectedNorm.equals(actualNorm)) {
                return;
            }
        }

        Assertions.fail(
                "Expected highlight '" + expected.getExternalName() + "' with text (ws-normalized):\n" +
                        expectedHighlightedText + "\n\n" +
                        "Target range: " + hostRange.getStartOffset() + ".." + hostRange.getEndOffset() + "\n" +
                        "Target text:\n" + target.text() + "\n\n" +
                        "Candidate highlighted intersections:\n  " +
                        (candidates.isEmpty() ? "<none>" : String.join("\n  ---\n  ", candidates))
        );
    }

    private static String formatHit(@NotNull HighlightInfo info,
                                    TextAttributesKey key,
                                    TextRange intersection) {
        return String.format(
                "[%d..%d] key=%s forced=%s intersection=[%d..%d]",
                info.getStartOffset(),
                info.getEndOffset(),
                key != null ? key.getExternalName() : "<null>",
                info.forcedTextAttributesKey != null,
                intersection.getStartOffset(),
                intersection.getEndOffset()
        );
    }

    private static String normalizeWs(String s) {
        return s.replaceAll("\\s+", " ").trim();
    }

    private static String preview(String s) {
        var oneLine = s.replace("\n", "\\n");
        return oneLine.length() <= 200 ? oneLine : oneLine.substring(0, 200) + "...";
    }

    private int countMergedSlices(@NotNull TextAttributesKey expected) {
        return mergedSlices(expected).size();
    }

    private @NotNull List<String> describeMergedSlices(@NotNull TextAttributesKey expected) {
        var doc = myFixture.getEditor().getDocument();
        var out = new ArrayList<String>();
        for (var r : mergedSlices(expected)) {
            out.add("[" + r.getStartOffset() + ".." + r.getEndOffset() + "] text=" + preview(doc.getText(r)));
        }
        return out;
    }

    private @NotNull List<TextRange> mergedSlices(@NotNull TextAttributesKey expected) {
        var hostRange = target.range();
        var infos = infos();

        var parts = new ArrayList<TextRange>();

        for (var info : infos) {
            var key = infoKey(info);
            if (!expected.equals(key)) {
                continue;
            }

            var r = new TextRange(info.getStartOffset(), info.getEndOffset());
            var inter = r.intersection(hostRange);
            if (inter == null || inter.isEmpty()) {
                continue;
            }

            parts.add(inter);
        }

        if (parts.isEmpty()) return List.of();

        parts.sort(Comparator.comparingInt(TextRange::getStartOffset).thenComparingInt(TextRange::getEndOffset));

        return getTextRanges(parts);
    }

    private static @NotNull ArrayList<TextRange> getTextRanges(ArrayList<TextRange> parts) {
        var merged = new ArrayList<TextRange>();
        var cur = parts.getFirst();

        for (var i = 1; i < parts.size(); i++) {
            var nxt = parts.get(i);

            if (nxt.getStartOffset() <= cur.getEndOffset()) {
                // overlap or touch
                cur = new TextRange(cur.getStartOffset(), Math.max(cur.getEndOffset(), nxt.getEndOffset()));
            } else {
                merged.add(cur);
                cur = nxt;
            }
        }
        merged.add(cur);
        return merged;
    }

    private static TextAttributesKey infoKey(HighlightInfo info) {
        return info.forcedTextAttributesKey != null
                ? info.forcedTextAttributesKey
                : (info.getHighlighter() != null ? info.getHighlighter().getTextAttributesKey() : null);
    }

    private SyntaxHighlighter syntaxHighlighter() {
        if (cachedSyntaxHighlighter == null) {
            cachedSyntaxHighlighter = SyntaxHighlighterFactory.getSyntaxHighlighter(
                    ConcordFileType.INSTANCE,
                    myFixture.getProject(),
                    myFixture.getFile().getVirtualFile()
            );
        }
        return cachedSyntaxHighlighter;
    }

    private static String describeKeys(Iterable<TextAttributesKey> keys) {
        var list = new ArrayList<String>();
        for (var k : keys) {
            list.add(k.getExternalName());
        }
        return list.isEmpty() ? "<none>" : list.stream().distinct().collect(Collectors.joining(", "));
    }

    private @NotNull Set<TextAttributesKey> daemonKeysCovering(int startOffset, int endOffset) {
        var out = new LinkedHashSet<TextAttributesKey>();
        for (var info : infos()) {
            if (info.getStartOffset() <= startOffset && info.getEndOffset() >= endOffset) {
                var key = infoKey(info);
                if (key != null) {
                    out.add(key);
                }
            }
        }
        return out;
    }

    private List<String> collectAllIntersectingHits(@NotNull TextRange hostRange) {
        var out = new ArrayList<String>();
        for (var info : infos()) {
            var key = infoKey(info);
            if (key == null) continue;

            var r = new TextRange(info.getStartOffset(), info.getEndOffset());
            var inter = r.intersection(hostRange);
            if (inter == null || inter.isEmpty()) continue;

            out.add(formatHit(info, key, inter));
        }
        return out.isEmpty() ? List.of("<none>") : out;
    }
}
