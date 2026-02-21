// SPDX-License-Identifier: Apache-2.0
package brig.concord.lexer;

import brig.concord.el.ElLexerAdapter;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapping lexer that decodes YAML double-quoted string escapes ({@code \\} &rarr; {@code \},
 * {@code \"} &rarr; {@code "}) before passing text to the EL lexer, then maps token positions
 * back to raw (YAML) offsets.
 * <p>
 * In YAML double-quoted strings, the EL expression body text contains YAML escape sequences.
 * The standard EL lexer cannot correctly tokenize this raw text because, e.g.,
 * raw {@code '\\'} is YAML for {@code \'} (EL escape of {@code '}) but the EL lexer
 * sees {@code \\} as a complete escape and treats the following {@code '} as a string delimiter.
 * <p>
 * Only {@code \\} and {@code \"} are decoded because these are the only YAML escapes that
 * affect EL tokenization boundaries. Other YAML escapes (e.g., {@code \n}, {@code \t}) are
 * passed through as-is — the EL lexer's {@code \\.} pattern handles them correctly.
 */
public class YamlDQElLexer extends LexerBase {

    private final ElLexerAdapter inner = new ElLexerAdapter();

    private CharSequence rawBuffer;
    private int rawEndOffset;

    /**
     * Maps decoded character positions to raw buffer positions.
     * {@code rawPositions[decodedIndex]} = raw offset of the decoded character.
     * {@code rawPositions[decodedLength]} = raw end offset (one past last raw char).
     */
    private int[] rawPositions;

    @Override
    public void start(@NotNull CharSequence buffer, int startOffset, int endOffset, int initialState) {
        rawBuffer = buffer;
        rawEndOffset = endOffset;

        // Decode YAML \\ and \" escapes, building position mapping
        var decoded = new StringBuilder(endOffset - startOffset);
        // Worst case: no escapes → decoded length = raw length; +1 for end sentinel
        var positions = new int[endOffset - startOffset + 1];
        int posCount = 0;

        int i = startOffset;
        while (i < endOffset) {
            char c = buffer.charAt(i);
            if (c == '\\' && i + 1 < endOffset) {
                char next = buffer.charAt(i + 1);
                if (next == '\\') {
                    // YAML \\ → decoded \
                    positions[posCount++] = i;
                    decoded.append('\\');
                    i += 2;
                } else if (next == '"') {
                    // YAML \" → decoded "
                    positions[posCount++] = i;
                    decoded.append('"');
                    i += 2;
                } else {
                    // Other YAML escape (\n, \t, etc.) — pass through both chars as-is
                    positions[posCount++] = i;
                    decoded.append(c);
                    positions[posCount++] = i + 1;
                    decoded.append(next);
                    i += 2;
                }
            } else {
                positions[posCount++] = i;
                decoded.append(c);
                i++;
            }
        }
        // End-of-text sentinel
        positions[posCount++] = i;

        // Trim to actual size
        rawPositions = new int[posCount];
        System.arraycopy(positions, 0, rawPositions, 0, posCount);

        // Start inner lexer on decoded text
        String decodedStr = decoded.toString();
        inner.start(decodedStr, 0, decodedStr.length(), initialState);
    }

    @Override
    public int getState() {
        return inner.getState();
    }

    @Override
    public @Nullable IElementType getTokenType() {
        return inner.getTokenType();
    }

    @Override
    public int getTokenStart() {
        return rawPositions[inner.getTokenStart()];
    }

    @Override
    public int getTokenEnd() {
        return rawPositions[inner.getTokenEnd()];
    }

    @Override
    public void advance() {
        inner.advance();
    }

    @Override
    public @NotNull CharSequence getBufferSequence() {
        return rawBuffer;
    }

    @Override
    public int getBufferEnd() {
        return rawEndOffset;
    }
}