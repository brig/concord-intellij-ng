// SPDX-License-Identifier: Apache-2.0
package brig.concord.lexer;

import brig.concord.el.ElLexerAdapter;
import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapping lexer that decodes YAML double-quoted string escape sequences before passing
 * text to the EL lexer, then maps token positions back to raw (YAML) offsets.
 * <p>
 * In YAML double-quoted strings, the EL expression body text contains YAML escape sequences.
 * The standard EL lexer cannot correctly tokenize this raw text because, e.g.,
 * raw {@code '\\'} is YAML for {@code \'} (EL escape of {@code '}) but the EL lexer
 * sees {@code \\} as a complete escape and treats the following {@code '} as a string delimiter.
 * Similarly, {@code \n} (backslash + letter n) is a YAML escape for a literal newline, but
 * the EL lexer would see {@code \} as a bad character and {@code n} as an identifier.
 * <p>
 * All standard YAML escape sequences are decoded to their actual characters.
 * Line folding ({@code \} followed by a newline) removes the newline and trims leading
 * whitespace on the continuation line.
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

        // Decode YAML escape sequences, building position mapping
        var decoded = new StringBuilder(endOffset - startOffset);
        // Worst case: no escapes → decoded length = raw length; +1 for end sentinel
        var positions = new int[endOffset - startOffset + 1];
        int posCount = 0;

        int i = startOffset;
        while (i < endOffset) {
            char c = buffer.charAt(i);
            if (c == '\\' && i + 1 < endOffset) {
                char next = buffer.charAt(i + 1);
                if (next == '\n' || next == '\r') {
                    // YAML escaped line break (line folding): removes the line break
                    // and trims leading whitespace on the continuation line
                    i += 2;
                    if (next == '\r' && i < endOffset && buffer.charAt(i) == '\n') {
                        i++; // skip \r\n
                    }
                    while (i < endOffset) {
                        char ws = buffer.charAt(i);
                        if (ws != ' ' && ws != '\t') {
                            break;
                        }
                        i++;
                    }
                    // No decoded character emitted
                } else {
                    // All other YAML escapes: decode to a single character
                    positions[posCount++] = i;
                    decoded.append(decodeYamlEscape(next));
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

    /**
     * Decodes a YAML double-quoted string escape character.
     * Given the character after {@code \}, returns the decoded character.
     *
     * @see <a href="https://yaml.org/spec/1.2.2/#rule-c-ns-esc-char">YAML 1.2 escape sequences</a>
     */
    private static char decodeYamlEscape(char c) {
        return switch (c) {
            case '0' -> '\0';       // null
            case 'a' -> '\u0007';   // bell
            case 'b' -> '\b';       // backspace
            case 't', '\t' -> '\t'; // tab (both \t and \<TAB>)
            case 'n' -> '\n';       // linefeed
            case 'v' -> '\u000B';   // vertical tab
            case 'f' -> '\f';       // form feed
            case 'r' -> '\r';       // carriage return
            case 'e' -> '\u001B';   // escape
            case ' ' -> ' ';        // space
            case '"' -> '"';        // double quote
            case '/' -> '/';        // slash
            case '\\' -> '\\';      // backslash
            case 'N' -> '\u0085';   // next line
            case '_' -> '\u00A0';   // non-breaking space
            case 'L' -> '\u2028';   // line separator
            case 'P' -> '\u2029';   // paragraph separator
            // Hex escapes (\xNN etc.) are rare in EL expressions;
            // return the escape letter as-is (the EL lexer will handle it gracefully)
            default -> c;
        };
    }
}