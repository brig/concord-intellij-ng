package brig.concord.highlighting;

import brig.concord.yaml.YAMLTokenTypes;
import brig.concord.yaml.lexer.YAMLFlexLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class ConcordSyntaxHighlighter extends SyntaxHighlighterBase {

    private static final TextAttributesKey[] EMPTY = TextAttributesKey.EMPTY_ARRAY;

    private static final Map<IElementType, TextAttributesKey[]> TOKEN_MAP = buildTokenMap();

    private static Map<IElementType, TextAttributesKey[]> buildTokenMap() {
        Map<IElementType, TextAttributesKey[]> m = new HashMap<>();

        // comments
        m.put(YAMLTokenTypes.COMMENT, new TextAttributesKey[]{
                ConcordHighlightingColors.COMMENT
        });

        // keys
        m.put(YAMLTokenTypes.SCALAR_KEY, new TextAttributesKey[]{
                ConcordHighlightingColors.USER_KEY
        });

        // strings / scalars
        TextAttributesKey[] string = {
                ConcordHighlightingColors.STRING
        };
        m.put(YAMLTokenTypes.SCALAR_STRING,  string);
        m.put(YAMLTokenTypes.SCALAR_DSTRING, string);
        m.put(YAMLTokenTypes.SCALAR_TEXT,    string);
        m.put(YAMLTokenTypes.SCALAR_LIST,    string);
        m.put(YAMLTokenTypes.TEXT,           string);

        // punctuation
        TextAttributesKey[] punctuation = {
                ConcordHighlightingColors.COLON
        };
        m.put(YAMLTokenTypes.COLON,           punctuation);
        m.put(YAMLTokenTypes.COMMA,           punctuation);
        m.put(YAMLTokenTypes.SEQUENCE_MARKER, punctuation);
        m.put(YAMLTokenTypes.QUESTION,        punctuation);

        // brackets
        TextAttributesKey[] brackets = {
                ConcordHighlightingColors.BRACKETS
        };
        m.put(YAMLTokenTypes.LBRACE,   brackets);
        m.put(YAMLTokenTypes.RBRACE,   brackets);
        m.put(YAMLTokenTypes.LBRACKET, brackets);
        m.put(YAMLTokenTypes.RBRACKET, brackets);

        return Map.copyOf(m);
    }

    @Override
    public @NotNull Lexer getHighlightingLexer() {
        return new YAMLFlexLexer();
    }

    @Override
    public TextAttributesKey @NotNull [] getTokenHighlights(IElementType tokenType) {
        return TOKEN_MAP.getOrDefault(tokenType, EMPTY);
    }
}
