package brig.concord.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerBase;
import com.intellij.lexer.RestartableLexer;
import com.intellij.lexer.TokenIterator;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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

    // Current segments from splitting. Null if not split.
    private List<Segment> segments;
    private int segmentIndex;

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
        segments = null;
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
        if (segments != null) {
            state |= (1 << 15);
        }
        return state;
    }

    @Override
    public @Nullable IElementType getTokenType() {
        if (segments != null) {
            if (segmentIndex < segments.size()) {
                return segments.get(segmentIndex).type;
            }
            return null;
        }
        return delegate.getTokenType();
    }

    @Override
    public int getTokenStart() {
        if (segments != null && segmentIndex < segments.size()) {
            return segments.get(segmentIndex).start;
        }
        return delegate.getTokenStart();
    }

    @Override
    public int getTokenEnd() {
        if (segments != null && segmentIndex < segments.size()) {
            return segments.get(segmentIndex).end;
        }
        return delegate.getTokenEnd();
    }

    @Override
    public void advance() {
        if (segments != null) {
            segmentIndex++;
            if (segmentIndex < segments.size()) {
                return;
            }
            // Done with segments — advance delegate and try splitting the next token
            segments = null;
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
            if (type == SCALAR_EOL || type == INDENT) {
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
        var tokenText = buffer.subSequence(tokenStart, tokenEnd).toString();

        var segs = buildSegments(type, tokenStart, tokenText);
        if (segs == null || segs.isEmpty()) {
            return;
        }

        segments = segs;
        segmentIndex = 0;
    }

    /**
     * Scan a token for ${ expressions, handling both complete and unclosed expressions.
     */
    private @Nullable List<Segment> buildSegments(
            @NotNull IElementType scalarType,
            int tokenStart,
            @NotNull String tokenText) {

        int n = tokenText.length();
        int pos = 0;
        List<Segment> result = null;

        while (pos < n) {
            int exprStart = indexOfUnescapedDollarBrace(tokenText, pos);
            if (exprStart < 0) {
                break;
            }

            if (result == null) {
                result = new ArrayList<>();
            }

            // Text before ${
            if (exprStart > pos) {
                result.add(new Segment(scalarType, tokenStart + pos, tokenStart + exprStart));
            }

            // ${ delimiter
            result.add(new Segment(EL_EXPR_START, tokenStart + exprStart, tokenStart + exprStart + 2));

            // Scan for closing }
            int bodyStart = exprStart + 2;
            int depth = 1;
            boolean sq = false;
            boolean dq = false;

            int j = bodyStart;
            boolean found = false;
            while (j < n) {
                char c = tokenText.charAt(j);

                if ((sq || dq) && c == '\\' && j + 1 < n) {
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
                            // Complete expression
                            if (j > bodyStart) {
                                result.add(new Segment(EL_EXPR_BODY, tokenStart + bodyStart, tokenStart + j));
                            }
                            result.add(new Segment(EL_EXPR_END, tokenStart + j, tokenStart + j + 1));
                            pos = j + 1;
                            found = true;
                            break;
                        }
                    }
                }
                j++;
            }

            if (!found) {
                if (CONTINUATION_TYPES.contains(scalarType)) {
                    // Block scalar / plain text: enter continuation mode for multi-line expressions
                    if (j > bodyStart) {
                        result.add(new Segment(EL_EXPR_BODY, tokenStart + bodyStart, tokenStart + j));
                    }
                    inExpression = true;
                    braceDepth = depth;
                    inSingleQuote = sq;
                    inDoubleQuote = dq;
                    pos = n;
                } else {
                    // Quoted string: unclosed expression — keep EL_EXPR_START + body so parser reports the error
                    if (j > bodyStart) {
                        result.add(new Segment(EL_EXPR_BODY, tokenStart + bodyStart, tokenStart + j));
                    }
                    pos = n;
                }
                break;
            }
        }

        // Text after last expression
        if (result != null && pos < n) {
            result.add(new Segment(scalarType, tokenStart + pos, tokenStart + n));
        }

        return result;
    }

    /**
     * Continue scanning within a continuation (multi-line expression).
     * The current delegate token is a splittable scalar in the middle of an unclosed ${...}.
     */
    private void splitContinuation(@NotNull IElementType scalarType) {
        int tokenStart = delegate.getTokenStart();
        int tokenEnd = delegate.getTokenEnd();
        String tokenText = buffer.subSequence(tokenStart, tokenEnd).toString();
        int n = tokenText.length();

        int j = 0;

        while (j < n) {
            char c = tokenText.charAt(j);

            if ((inSingleQuote || inDoubleQuote) && c == '\\' && j + 1 < n) {
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
                        var result = new ArrayList<Segment>();

                        // Body fragment before }
                        if (j > 0) {
                            result.add(new Segment(EL_EXPR_BODY, tokenStart, tokenStart + j));
                        }

                        // } delimiter
                        result.add(new Segment(EL_EXPR_END, tokenStart + j, tokenStart + j + 1));

                        resetContinuation();

                        // Remaining text after } — may contain more ${...} expressions
                        int remaining = j + 1;
                        if (remaining < n) {
                            String rest = tokenText.substring(remaining);
                            var moreSegs = buildSegments(scalarType, tokenStart + remaining, rest);
                            if (moreSegs != null) {
                                result.addAll(moreSegs);
                            } else {
                                result.add(new Segment(scalarType, tokenStart + remaining, tokenEnd));
                            }
                        }

                        segments = result;
                        segmentIndex = 0;
                        return;
                    }
                }
            }
            j++;
        }

        // Still unclosed — entire token is EL_EXPR_BODY continuation
        var result = new ArrayList<Segment>();
        result.add(new Segment(EL_EXPR_BODY, tokenStart, tokenEnd));
        segments = result;
        segmentIndex = 0;
    }

    private void resetContinuation() {
        inExpression = false;
        braceDepth = 0;
        inSingleQuote = false;
        inDoubleQuote = false;
    }

    /**
     * Find the next unescaped "${" in text starting from fromIndex.
     */
    private static int indexOfUnescapedDollarBrace(@NotNull String text, int fromIndex) {
        int n = text.length();
        int idx = fromIndex;
        while (idx <= n - 2) {
            int p = text.indexOf("${", idx);
            if (p < 0) {
                return -1;
            }
            if (!isEscaped(text, p)) {
                return p;
            }
            idx = p + 1;
        }
        return -1;
    }

    /**
     * Returns true if character at position 'pos' is escaped by an odd number of backslashes.
     */
    private static boolean isEscaped(@NotNull String s, int pos) {
        int bs = 0;
        int i = pos - 1;
        while (i >= 0 && s.charAt(i) == '\\') {
            bs++;
            i--;
        }
        return (bs % 2) == 1;
    }

    private record Segment(@NotNull IElementType type, int start, int end) {
    }
}
