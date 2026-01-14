package brig.concord.highlighting;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public final class ConcordHighlightingColors {

    /**
     * Top-level DSL section keys: flows, configuration, forms, triggers, imports, profiles.
     */
    public static final TextAttributesKey DSL_SECTION = createTextAttributesKey(
            "CONCORD_DSL_SECTION",
            DefaultLanguageHighlighterColors.KEYWORD
    );

    /**
     * Step keywords: task, call, log, script, set, checkpoint, suspend, throw, form, expr, return, etc.
     */
    public static final TextAttributesKey STEP_KEYWORD = createTextAttributesKey(
            "CONCORD_STEP_KEYWORD",
            DefaultLanguageHighlighterColors.FUNCTION_CALL
    );

    /**
     * DSL kinds in imports/triggers that declare which schema applies:
     * imports: git, mvn, ...
     * triggers: github, cron, manual, oneops, ...
     */
    public static final TextAttributesKey DSL_KIND = createTextAttributesKey(
            "CONCORD_DSL_KIND",
            DefaultLanguageHighlighterColors.FUNCTION_CALL
    );

    /**
     * Predefined DSL keys (schema-defined keys) across all sections:
     * configuration keys (runtime/entryPoint/...),
     * step keys (in/out/retry/loop/...),
     * imports keys (url/path/version/...),
     * triggers keys (entryPoint/ignoreEmptyPush/...).
     */
    public static final TextAttributesKey DSL_KEY = createTextAttributesKey(
            "CONCORD_DSL_KEY",
            DefaultLanguageHighlighterColors.PARAMETER
    );

    /**
     * DSL label (typically the "name" key/value style).
     */
    public static final TextAttributesKey DSL_LABEL = createTextAttributesKey(
            "CONCORD_DSL_LABEL",
            DefaultLanguageHighlighterColors.PARAMETER
    );

    /**
     * Flow identifiers (flow names): main, deploy, cleanup, etc.
     */
    public static final TextAttributesKey FLOW_IDENTIFIER = createTextAttributesKey(
            "CONCORD_FLOW_IDENTIFIER",
            DefaultLanguageHighlighterColors.FUNCTION_DECLARATION
    );

    /**
     * Target identifiers (values after task:/call:/entryPoint:): downloadData, otherFlow, etc.
     */
    public static final TextAttributesKey TARGET_IDENTIFIER = createTextAttributesKey(
            "CONCORD_TARGET_IDENTIFIER",
            DefaultLanguageHighlighterColors.FUNCTION_CALL
    );

    /**
     * User-defined keys (not recognized as predefined DSL keys).
     */
    public static final TextAttributesKey USER_KEY = createTextAttributesKey(
            "CONCORD_USER_KEY",
            DefaultLanguageHighlighterColors.IDENTIFIER
    );

    /**
     * Expression syntax: ${...}
     */
    public static final TextAttributesKey EXPRESSION = createTextAttributesKey(
            "CONCORD_EXPRESSION",
            DefaultLanguageHighlighterColors.TEMPLATE_LANGUAGE_COLOR
    );

    /**
     * String values (quoted and unquoted).
     */
    public static final TextAttributesKey STRING = createTextAttributesKey(
            "CONCORD_STRING",
            DefaultLanguageHighlighterColors.STRING
    );

    /**
     * Numeric values.
     */
    public static final TextAttributesKey NUMBER = createTextAttributesKey(
            "CONCORD_NUMBER",
            DefaultLanguageHighlighterColors.NUMBER
    );

    /**
     * Boolean values: true, false.
     */
    public static final TextAttributesKey BOOLEAN = createTextAttributesKey(
            "CONCORD_BOOLEAN",
            DefaultLanguageHighlighterColors.KEYWORD
    );

    /**
     * Null values: null, ~
     */
    public static final TextAttributesKey NULL = createTextAttributesKey(
            "CONCORD_NULL",
            DefaultLanguageHighlighterColors.PREDEFINED_SYMBOL
    );

    /**
     * Comments.
     */
    public static final TextAttributesKey COMMENT = createTextAttributesKey(
            "CONCORD_COMMENT",
            DefaultLanguageHighlighterColors.LINE_COMMENT
    );

    /**
     * Colon separator.
     */
    public static final TextAttributesKey COLON = createTextAttributesKey(
            "CONCORD_COLON",
            DefaultLanguageHighlighterColors.OPERATION_SIGN
    );

    /**
     * Brackets and braces: [], {}.
     */
    public static final TextAttributesKey BRACKETS = createTextAttributesKey(
            "CONCORD_BRACKETS",
            DefaultLanguageHighlighterColors.BRACKETS
    );

    public static final TextAttributesKey BAD_CHARACTER = createTextAttributesKey(
            "CONCORD_BAD_CHARACTER",
            HighlighterColors.BAD_CHARACTER
    );

    private ConcordHighlightingColors() {
    }
}
