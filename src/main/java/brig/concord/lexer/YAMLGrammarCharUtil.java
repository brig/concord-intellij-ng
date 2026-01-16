package brig.concord.lexer;

import com.intellij.openapi.util.text.StringUtil;

public final class YAMLGrammarCharUtil {

    private static final String COMMON_SPACE_CHARS = "\n\r\t ";

    private YAMLGrammarCharUtil() {
    }

    public static boolean isSpaceLike(char c) {
        return c == ' ' || c == '\t';
    }

    public static boolean isNonSpaceChar(char c) {
        return !StringUtil.containsChar(COMMON_SPACE_CHARS, c);
    }
}
