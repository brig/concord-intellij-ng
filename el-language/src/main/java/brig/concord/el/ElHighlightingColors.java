package brig.concord.el;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public final class ElHighlightingColors {

    public static final TextAttributesKey KEYWORD = createTextAttributesKey(
            "EL_KEYWORD",
            DefaultLanguageHighlighterColors.KEYWORD
    );

    public static final TextAttributesKey STRING = createTextAttributesKey(
            "EL_STRING",
            DefaultLanguageHighlighterColors.STRING
    );

    public static final TextAttributesKey NUMBER = createTextAttributesKey(
            "EL_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
    );

    public static final TextAttributesKey IDENTIFIER = createTextAttributesKey(
            "EL_IDENTIFIER",
            DefaultLanguageHighlighterColors.IDENTIFIER
    );

    public static final TextAttributesKey OPERATION_SIGN = createTextAttributesKey(
            "EL_OPERATION_SIGN",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
    );

    public static final TextAttributesKey PARENTHESES = createTextAttributesKey(
            "EL_PARENTHESES",
            DefaultLanguageHighlighterColors.PARENTHESES
    );

    public static final TextAttributesKey BRACKETS = createTextAttributesKey(
            "EL_BRACKETS",
            DefaultLanguageHighlighterColors.BRACKETS
    );

    public static final TextAttributesKey BRACES = createTextAttributesKey(
            "EL_BRACES",
            DefaultLanguageHighlighterColors.BRACES
    );

    public static final TextAttributesKey DOT = createTextAttributesKey(
            "EL_DOT",
            DefaultLanguageHighlighterColors.DOT
    );

    public static final TextAttributesKey COMMA = createTextAttributesKey(
            "EL_COMMA",
            DefaultLanguageHighlighterColors.COMMA
    );

    public static final TextAttributesKey SEMICOLON = createTextAttributesKey(
            "EL_SEMICOLON",
            DefaultLanguageHighlighterColors.SEMICOLON
    );

    public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey(
            "EL_BAD_CHARACTER",
            HighlighterColors.BAD_CHARACTER
    );

    private ElHighlightingColors() {
    }
}
