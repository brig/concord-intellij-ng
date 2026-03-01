// SPDX-License-Identifier: Apache-2.0
package brig.concord.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerBase;
import com.intellij.lexer.RestartableLexer;
import com.intellij.lexer.TokenIterator;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static brig.concord.lexer.ConcordElTokenTypes.*;
import static brig.concord.yaml.YAMLTokenTypes.*;

/**
 * A wrapper lexer that intercepts scalar tokens and splits them at ${...} expression boundaries.
 * <p>
 * For single-line expressions, splits e.g. {@code "prefix ${x + 1} suffix"} into:
 * <pre>
 *   SCALAR_DSTRING  "prefix "
 *   EL_EXPR_START   "${"
 *   EL_EXPR_BODY    "x + 1"
 *   EL_EXPR_END     "}"
 *   SCALAR_DSTRING  " suffix"
 * </pre>
 * <p>
 * For multi-line expressions in block scalars, enters continuation mode:
 * <pre>
 *   SCALAR_LIST     "text "
 *   EL_EXPR_START   "${"
 *   EL_EXPR_BODY    "a +"          (unclosed — continuation mode)
 *   SCALAR_EOL      "\n"           (passthrough)
 *   INDENT          "  "           (passthrough)
 *   EL_EXPR_BODY    "b"            (continuation fragment)
 *   EL_EXPR_END     "}"
 *   SCALAR_LIST     " more"
 * </pre>
 * The parser collapses EL_EXPR_BODY + intervening tokens into a single EL_EXPR chameleon node.
 */
public class ExpressionSplittingLexer extends LexerBase implements RestartableLexer {

    private static final TokenSet SPLITTABLE = TokenSet.create(
            TEXT, SCALAR_DSTRING, SCALAR_STRING, SCALAR_LIST, SCALAR_TEXT
    );

    /** Token types where multi-line continuation is valid (block scalars and plain text). */
    private static final TokenSet CONTINUATION_TYPES = TokenSet.create(
            TEXT, SCALAR_LIST, SCALAR_TEXT
    );

    private final Lexer delegate;

    private CharSequence buffer;
    private int bufferEnd;

    // Reusable segment storage — avoids per-split ArrayList/Segment allocation.
    // Parallel arrays hold (type, start, end) for each segment.
    private IElementType[] segTypes = new IElementType[8];
    private int[] segStarts = new int[8];
    private int[] segEnds = new int[8];
    private int segCount;
    private int segmentIndex;
    private boolean splitting;

    // Reusable scan result fields — avoids BraceScanResult record allocation
    private boolean scanFound;
    private int scanEndPos;
    private int scanDepth;
    private boolean scanInSQ;
    private boolean scanInDQ;

    // Continuation state for multi-line expressions
    private boolean inExpression;
    private int braceDepth;
    private boolean inSingleQuote;
    private boolean inDoubleQuote;

    public ExpressionSplittingLexer(@NotNull Lexer delegate) {
        this.delegate = delegate;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        this.buffer = buffer;
        this.bufferEnd = endOffset;

        // Decode state: bits 0-7 = delegate state, bit 8 = inExpression,
        // bits 9-12 = braceDepth, bit 13 = inSingleQuote, bit 14 = inDoubleQuote,
        // bit 15 = mid-split (not decoded — only used by isRestartableState)
        int delegateState = initialState & 0xFF;
        inExpression = (initialState & (1 << 8)) != 0;
        braceDepth = (initialState >> 9) & 0xF;
        inSingleQuote = (initialState & (1 << 13)) != 0;
        inDoubleQuote = (initialState & (1 << 14)) != 0;

        delegate.start(buffer, startOffset, endOffset, delegateState);
        splitting = false;
        segCount = 0;
        segmentIndex = 0;
        trySplit();
    }

    @Override
    public int getState() {
        int delegateState = delegate.getState();
        assert (delegateState & 0xFF) == delegateState : "Delegate state exceeds 8 bits: " + delegateState;
        int state = delegateState & 0xFF;
        if (inExpression) {
            state |= (1 << 8);
        }
        state |= (braceDepth & 0xF) << 9;
        if (inSingleQuote) {
            state |= (1 << 13);
        }
        if (inDoubleQuote) {
            state |= (1 << 14);
        }
        // Mark in-split: when emitting segments from a split delegate token, the
        // highlighter must not restart at any segment offset because (a) the delegate
        // cannot be restarted mid-token, and (b) for continuation splits,
        // resetContinuation() has already cleared inExpression before segments are emitted.
        if (splitting) {
            state |= (1 << 15);
        }
        return state;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        if (splitting) {
            if (segmentIndex < segCount) {
                return segTypes[segmentIndex];
            }
            return null;
        }
        return delegate.getTokenType();
    }

    @Override
    public int getTokenStart() {
        if (splitting && segmentIndex < segCount) {
            return segStarts[segmentIndex];
        }
        return delegate.getTokenStart();
    }

    @Override
    public int getTokenEnd() {
        if (splitting && segmentIndex < segCount) {
            return segEnds[segmentIndex];
        }
        return delegate.getTokenEnd();
    }

    @Override
    public void advance() {
        if (splitting) {
            segmentIndex++;
            if (segmentIndex < segCount) {
                return;
            }
            // Done with segments — advance delegate and try splitting the next token
            splitting = false;
            segCount = 0;
            segmentIndex = 0;
        }
        delegate.advance();
        trySplit();
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return buffer;
    }

    @Override
    public int getBufferEnd() {
        return bufferEnd;
    }

    @Override
    public int getStartState() {
        return 0;
    }

    // Delegate states at or above this threshold depend on internal JFlex variables
    // (myBlockScalarType, myPrevElementIndent, myFlowDocSectionIndent, etc.) that are
    // destroyed by cleanMyState() on restart.  States below this (YYINITIAL through
    // KEY_MODE) survive cleanMyState() well enough to produce contiguous tokens.
    private static final int DELEGATE_STATE_RESTARTABLE_LIMIT = 16; // BS_HEADER_TAIL_STATE

    @Override
    public boolean isRestartableState(int state) {
        int delegateState = state & 0xFF;
        // Not restartable when:
        //  - inside a multi-line expression continuation (bit 8), or
        //  - emitting split segments (bit 15) — delegate can't restart mid-token, or
        //  - delegate is in block-scalar / flow-doc state that needs internal vars
        return (state & ((1 << 8) | (1 << 15))) == 0
                && delegateState < DELEGATE_STATE_RESTARTABLE_LIMIT;
    }

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset,
                      int initialState, TokenIterator tokenIterator) {
        start(buffer, startOffset, endOffset, initialState);
    }

    /**
     * If the current delegate token is a splittable scalar type that contains ${...},
     * build the segment list. Otherwise do nothing (passthrough).
     * In continuation mode, handles SCALAR_EOL/INDENT passthrough and continuation fragments.
     */
    private void trySplit() {
        var type = delegate.getTokenType();
        if (type == null) {
            return;
        }

        if (inExpression) {
            // In continuation mode
            if (type == SCALAR_EOL || type == EOL || type == INDENT) {
                // Passthrough — these tokens pass through as-is in continuation mode
                return;
            }

            if (SPLITTABLE.contains(type)) {
                // Continue scanning for closing }
                splitContinuation(type);
                return;
            }

            // Left the block scalar context — reset continuation
            resetContinuation();
            // Fall through to normal processing
        }

        if (!SPLITTABLE.contains(type)) {
            return;
        }

        int tokenStart = delegate.getTokenStart();
        int tokenEnd = delegate.getTokenEnd();

        if (!containsDollarBrace(buffer, tokenStart, tokenEnd)) {
            return;
        }

        segCount = 0;
        if (buildSegments(type, tokenStart, tokenEnd)) {
            splitting = true;
            segmentIndex = 0;
        }
    }

    /**
     * Scan a buffer range for ${ expressions, handling both complete and unclosed expressions.
     * Appends segments to the reusable segment arrays.
     * Works directly with {@link #buffer} using absolute offsets to avoid String allocations.
     *
     * @return true if any expressions were found and segments were added
     */
    private boolean buildSegments(
            @NotNull IElementType scalarType,
            int from,
            int to) {

        int pos = from;
        boolean added = false;

        while (pos < to) {
            int exprStart = indexOfUnescapedDollarBrace(buffer, pos, to);
            if (exprStart < 0) {
                break;
            }

            added = true;

            // Text before ${
            if (exprStart > pos) {
                addSegment(scalarType, pos, exprStart);
            }

            // ${ delimiter
            addSegment(EL_EXPR_START, exprStart, exprStart + 2);

            int bodyStart = exprStart + 2;
            scanForClosingBrace(bodyStart, to, scalarType == SCALAR_DSTRING);

            if (scanFound) {
                if (scanEndPos > bodyStart) {
                    addSegment(EL_EXPR_BODY, bodyStart, scanEndPos);
                }
                addSegment(EL_EXPR_END, scanEndPos, scanEndPos + 1);
                pos = scanEndPos + 1;
            } else {
                if (scanEndPos > bodyStart) {
                    addSegment(EL_EXPR_BODY, bodyStart, scanEndPos);
                }
                if (CONTINUATION_TYPES.contains(scalarType)) {
                    // Block scalar / plain text: enter continuation mode for multi-line expressions
                    inExpression = true;
                    braceDepth = scanDepth;
                    inSingleQuote = scanInSQ;
                    inDoubleQuote = scanInDQ;
                }
                pos = to;
                break;
            }
        }

        // Text after last expression
        if (added && pos < to) {
            addSegment(scalarType, pos, to);
        }

        return added;
    }

    /**
     * Scan for the closing {@code }} of an EL expression, tracking brace depth and EL string
     * quoting state. In YAML double-quoted scalars ({@code yamlDQ=true}), YAML escape sequences
     * ({@code \\}, {@code \"}) are decoded before EL-level processing.
     * <p>
     * Results are written to {@link #scanFound}, {@link #scanEndPos}, {@link #scanDepth},
     * {@link #scanInSQ}, {@link #scanInDQ} to avoid allocating a result object.
     *
     * @param from   start of expression body (just after <code>${</code>)
     * @param to     end of the scalar token range
     * @param yamlDQ true if the enclosing scalar is a YAML double-quoted string
     */
    private void scanForClosingBrace(int from, int to, boolean yamlDQ) {
        int depth = 1;
        boolean sq = false;
        boolean dq = false;
        // When inside a YAML double-quoted string, a decoded \ (from raw \\)
        // may escape the next decoded character (e.g., raw YAML \\\" → decoded \", an EL escape).
        boolean yamlDecodedBackslash = false;

        int j = from;
        while (j < to) {
            char c = buffer.charAt(j);

            // In YAML double-quoted strings, \ is always a YAML escape prefix.
            // Handle it before EL-level logic to correctly distinguish
            // YAML escapes (like \") from EL escapes.
            if (yamlDQ && c == '\\' && j + 1 < to) {
                char next = buffer.charAt(j + 1);
                j += 2;

                if (next == '\\') {
                    // YAML \\, decoded: \
                    // Inside an EL string, track whether this decoded \
                    // will escape the next decoded character.
                    if (sq || dq) {
                        yamlDecodedBackslash = !yamlDecodedBackslash;
                    }
                } else if (next == '"') {
                    // YAML \", decoded: "
                    if (yamlDecodedBackslash) {
                        // Preceded by decoded \ → EL escape \" → don't toggle
                        yamlDecodedBackslash = false;
                    } else if (!sq) {
                        dq = !dq;
                    }
                } else if (next == '\n' || next == '\r') {
                    // YAML line folding: \<newline> produces no character.
                    // Preserve yamlDecodedBackslash — a pending decoded \ is still pending.
                    if (next == '\r' && j < to && buffer.charAt(j) == '\n') {
                        j++; // skip \r\n
                    }
                    while (j < to) {
                        char ws = buffer.charAt(j);
                        if (ws != ' ' && ws != '\t') {
                            break;
                        }
                        j++;
                    }
                } else {
                    // Other YAML escape (\n, \t, etc.) — decoded char is not \ or "
                    yamlDecodedBackslash = false;
                }
                continue;
            }

            // A decoded \ from a YAML \\ escapes the next (non-YAML-escape) character
            if (yamlDecodedBackslash) {
                yamlDecodedBackslash = false;
                j++;
                continue;
            }

            // EL-level escape: \ inside EL-quoted strings (non-YAML-DQ contexts)
            if ((sq || dq) && c == '\\' && j + 1 < to) {
                j += 2;
                continue;
            }

            if (!dq && c == '\'') {
                sq = !sq;
                j++;
                continue;
            }
            if (!sq && c == '"') {
                dq = !dq;
                j++;
                continue;
            }

            if (!sq && !dq) {
                if (c == '{') {
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0) {
                        scanFound = true;
                        scanEndPos = j;
                        scanDepth = 0;
                        scanInSQ = false;
                        scanInDQ = false;
                        return;
                    }
                }
            }
            j++;
        }

        scanFound = false;
        scanEndPos = j;
        scanDepth = depth;
        scanInSQ = sq;
        scanInDQ = dq;
    }

    /**
     * Continue scanning within a continuation (multi-line expression).
     * The current delegate token is a splittable scalar in the middle of an unclosed ${...}.
     * Works directly with {@link #buffer} using absolute offsets to avoid String allocations.
     */
    private void splitContinuation(@NotNull IElementType scalarType) {
        int tokenStart = delegate.getTokenStart();
        int tokenEnd = delegate.getTokenEnd();

        int j = tokenStart;

        while (j < tokenEnd) {
            char c = buffer.charAt(j);

            if ((inSingleQuote || inDoubleQuote) && c == '\\' && j + 1 < tokenEnd) {
                j += 2;
                continue;
            }

            if (!inDoubleQuote && c == '\'') {
                inSingleQuote = !inSingleQuote;
                j++;
                continue;
            }
            if (!inSingleQuote && c == '"') {
                inDoubleQuote = !inDoubleQuote;
                j++;
                continue;
            }

            if (!inSingleQuote && !inDoubleQuote) {
                if (c == '{') {
                    braceDepth++;
                } else if (c == '}') {
                    braceDepth--;
                    if (braceDepth == 0) {
                        // Found closing } — end continuation
                        segCount = 0;

                        // Body fragment before }
                        if (j > tokenStart) {
                            addSegment(EL_EXPR_BODY, tokenStart, j);
                        }

                        // } delimiter
                        addSegment(EL_EXPR_END, j, j + 1);

                        resetContinuation();

                        // Remaining text after } — may contain more ${...} expressions
                        int remaining = j + 1;
                        if (remaining < tokenEnd) {
                            if (!buildSegments(scalarType, remaining, tokenEnd)) {
                                addSegment(scalarType, remaining, tokenEnd);
                            }
                        }

                        splitting = true;
                        segmentIndex = 0;
                        return;
                    }
                }
            }
            j++;
        }

        // Still unclosed — entire token is EL_EXPR_BODY continuation
        segCount = 0;
        addSegment(EL_EXPR_BODY, tokenStart, tokenEnd);
        splitting = true;
        segmentIndex = 0;
    }

    private void addSegment(@NotNull IElementType type, int start, int end) {
        if (segCount == segTypes.length) {
            int newLen = segTypes.length * 2;
            segTypes = Arrays.copyOf(segTypes, newLen);
            segStarts = Arrays.copyOf(segStarts, newLen);
            segEnds = Arrays.copyOf(segEnds, newLen);
        }
        segTypes[segCount] = type;
        segStarts[segCount] = start;
        segEnds[segCount] = end;
        segCount++;
    }

    private void resetContinuation() {
        inExpression = false;
        braceDepth = 0;
        inSingleQuote = false;
        inDoubleQuote = false;
    }

    /**
     * Quick check for "${" presence in the buffer range without allocating a String.
     */
    private static boolean containsDollarBrace(@NotNull CharSequence buf, int from, int to) {
        for (int i = from; i < to - 1; i++) {
            if (buf.charAt(i) == '$' && buf.charAt(i + 1) == '{') {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the next unescaped "${" in the buffer range [from, to).
     * Returns an absolute offset into buf, or -1 if not found.
     */
    private static int indexOfUnescapedDollarBrace(@NotNull CharSequence buf, int from, int to) {
        for (int i = from; i < to - 1; i++) {
            if (buf.charAt(i) == '$' && buf.charAt(i + 1) == '{') {
                if (!isEscaped(buf, i)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns true if character at position 'pos' is escaped by an odd number of backslashes.
     */
    private static boolean isEscaped(@NotNull CharSequence buf, int pos) {
        int bs = 0;
        int i = pos - 1;
        while (i >= 0 && buf.charAt(i) == '\\') {
            bs++;
            i--;
        }
        return (bs % 2) == 1;
    }
}