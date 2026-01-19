package brig.concord.lexer;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FlowDocLexerTest {

    @Test
    public void testFlowDocTokens() {
        var yaml = """
            flows:
              ##
              # Process S3 files
              # in:
              #   s3Bucket: string, mandatory, S3 bucket name
              # out:
              #   processed: int, mandatory, Files processed count
              ##
              processS3:
                - task: s3
            """;

        var tokens = tokenize(yaml, 200);

        // Check that # is now returned as FLOW_DOC_COMMENT_PREFIX
        var prefixCount = countTokens(tokens, "FLOW_DOC_COMMENT_PREFIX");
        assertEquals(5, prefixCount,
                "Should find 5 FLOW_DOC_COMMENT_PREFIX tokens (one per comment line). Output:\n" + formatTokens(tokens));

        assertEquals(2, countTokens(tokens, "FLOW_DOC_MARKER"),
                "Should find 2 FLOW_DOC_MARKER tokens. Output:\n" + formatTokens(tokens));
    }

    @Test
    public void testMultipleFlowDocs() {
        var yaml = """
            flows:
              ##
              # First flow
              # in:
              #   param1: string, mandatory
              ##
              flow1:
                - log: "1"

              ##
              # Second flow
              # in:
              #   param2: int, optional
              ##
              flow2:
                - log: "2"
            """;

        var tokens = tokenize(yaml);
        var markerCount = countTokens(tokens, "FLOW_DOC_MARKER");

        assertEquals(4, markerCount,
                "Should find 4 FLOW_DOC_MARKER tokens (2 open + 2 close). Output:\n" + formatTokens(tokens));
    }

    @Test
    public void testCustomTagsInFlowDoc() {
        var yaml = """
            flows:
              ##
              # Process S3 files
              # in:
              #   s3Bucket: string, mandatory, S3 bucket name
              # tags: not in param, just user tag
              ##
              processS3:
                - task: s3
            """;

        var tokens = tokenize(yaml, 200);

        var markerCount = countTokens(tokens, "FLOW_DOC_MARKER");
        assertEquals(2, markerCount,
                "Should find 2 FLOW_DOC_MARKER tokens. Output:\n" + formatTokens(tokens));

        // Check that "tags" is NOT parsed as FLOW_DOC_PARAM_NAME
        var paramNameTokens = tokens.stream()
                .filter(t -> t.type().toString().equals("FLOW_DOC_PARAM_NAME"))
                .toList();

        assertEquals(1, paramNameTokens.size(),
                "Should find only 1 param name (s3Bucket). Found: " + paramNameTokens + "\n\nAll tokens:\n" + formatTokens(tokens));
    }

    @Test
    public void testFlowDocWithEmptyLines() {
        var yaml = """
            flows:
              ##
              # Process S3 files
              #
              # in:
              #   s3Bucket: string, mandatory, S3 bucket name
              #
              # out:
              #   processed: int, mandatory, Files processed count
              #
              # tags: 1, 2, 3
              ##
              processS3:
                - task: s3
            """;

        var tokens = tokenize(yaml);
        var markerCount = countTokens(tokens, "FLOW_DOC_MARKER");

        assertEquals(2, markerCount,
                "Should find 2 FLOW_DOC_MARKER tokens (1 open + 1 close). Output:\n" + formatTokens(tokens));
    }

    @Test
    public void testCommentPrefixIsSeparateToken() {
        var yaml = """
            flows:
              ##
              # Description
              ##
              myFlow:
                - log: "test"
            """;

        var tokens = tokenize(yaml, 50);

        // Find FLOW_DOC_COMMENT_PREFIX token
        var prefixTokens = tokens.stream()
                .filter(t -> t.type().toString().equals("FLOW_DOC_COMMENT_PREFIX"))
                .toList();

        assertEquals(1, prefixTokens.size(),
                "Should find 1 FLOW_DOC_COMMENT_PREFIX token. Output:\n" + formatTokens(tokens));

        // The prefix should be just "#"
        assertEquals("#", prefixTokens.getFirst().text(),
                "FLOW_DOC_COMMENT_PREFIX should be just '#'");

        // After prefix should come whitespace (the space after #)
        var prefixIndex = tokens.indexOf(prefixTokens.getFirst());
        var nextToken = tokens.get(prefixIndex + 1);
        assertEquals("whitespace", nextToken.type().toString(),
                "Token after FLOW_DOC_COMMENT_PREFIX should be whitespace. Output:\n" + formatTokens(tokens));

        // Then FLOW_DOC_CONTENT with the description
        var contentToken = tokens.get(prefixIndex + 2);
        assertEquals("FLOW_DOC_CONTENT", contentToken.type().toString(),
                "Token after whitespace should be FLOW_DOC_CONTENT. Output:\n" + formatTokens(tokens));
        assertEquals("Description", contentToken.text(),
                "FLOW_DOC_CONTENT should contain description text");
    }

    @Test
    public void testHashWithoutSpaceInDescription() {
        // Edge case: # followed by text without space (fallback case)
        var yaml = """
            flows:
              ##
              #NoSpace
              ##
              myFlow:
                - log: "test"
            """;

        var tokens = tokenize(yaml, 50);

        // Should still find markers
        assertEquals(2, countTokens(tokens, "FLOW_DOC_MARKER"),
                "Should find 2 FLOW_DOC_MARKER tokens. Output:\n" + formatTokens(tokens));

        // Should find comment prefix
        assertEquals(1, countTokens(tokens, "FLOW_DOC_COMMENT_PREFIX"),
                "Should find 1 FLOW_DOC_COMMENT_PREFIX token. Output:\n" + formatTokens(tokens));

        // NoSpace should be parsed as content
        var contentTokens = tokens.stream()
                .filter(t -> t.type().toString().equals("FLOW_DOC_CONTENT"))
                .toList();
        assertTrue(contentTokens.stream().anyMatch(t -> t.text().equals("NoSpace")),
                "Should find 'NoSpace' as FLOW_DOC_CONTENT. Output:\n" + formatTokens(tokens));
    }

    @Test
    public void testHashWithoutSpaceInParams() {
        // Edge case: # followed by text without space inside params section (fallback case)
        var yaml = """
            flows:
              ##
              # in:
              #   param1: string, mandatory
              #NoSpaceTag
              ##
              myFlow:
                - log: "test"
            """;

        var tokens = tokenize(yaml, 100);

        // Should still find markers
        assertEquals(2, countTokens(tokens, "FLOW_DOC_MARKER"),
                "Should find 2 FLOW_DOC_MARKER tokens. Output:\n" + formatTokens(tokens));

        // param1 should be parsed as param name
        var paramNameTokens = tokens.stream()
                .filter(t -> t.type().toString().equals("FLOW_DOC_PARAM_NAME"))
                .toList();
        assertEquals(1, paramNameTokens.size(),
                "Should find 1 FLOW_DOC_PARAM_NAME token. Output:\n" + formatTokens(tokens));

        // NoSpaceTag should be parsed as FLOW_DOC_TEXT (fallback)
        var textTokens = tokens.stream()
                .filter(t -> t.type().toString().equals("FLOW_DOC_TEXT"))
                .toList();
        assertTrue(textTokens.stream().anyMatch(t -> t.text().equals("NoSpaceTag")),
                "Should find 'NoSpaceTag' as FLOW_DOC_TEXT. Output:\n" + formatTokens(tokens));
    }

    @Test
    public void testMandatoryAndOptionalTokens() {
        var yaml = """
            flows:
              ##
              # in:
              #   param1: string, mandatory, Required param
              #   param2: int, optional, Optional param
              ##
              myFlow:
                - log: "test"
            """;

        var tokens = tokenize(yaml, 100);

        // Check FLOW_DOC_MANDATORY token
        var mandatoryTokens = tokens.stream()
                .filter(t -> t.type().toString().equals("FLOW_DOC_MANDATORY"))
                .toList();
        assertEquals(1, mandatoryTokens.size(),
                "Should find 1 FLOW_DOC_MANDATORY token. Output:\n" + formatTokens(tokens));
        assertEquals("mandatory", mandatoryTokens.getFirst().text(),
                "FLOW_DOC_MANDATORY token should contain 'mandatory'");

        // Check FLOW_DOC_OPTIONAL token
        var optionalTokens = tokens.stream()
                .filter(t -> t.type().toString().equals("FLOW_DOC_OPTIONAL"))
                .toList();
        assertEquals(1, optionalTokens.size(),
                "Should find 1 FLOW_DOC_OPTIONAL token. Output:\n" + formatTokens(tokens));
        assertEquals("optional", optionalTokens.getFirst().text(),
                "FLOW_DOC_OPTIONAL token should contain 'optional'");
    }

    @Test
    public void testEmptyCommentLine() {
        // Edge case: just # with nothing after it
        var yaml = """
            flows:
              ##
              # Description
              #
              # in:
              #   param1: string, mandatory
              ##
              myFlow:
                - log: "test"
            """;

        var tokens = tokenize(yaml, 100);

        // Should find 4 comment prefixes (Description, empty, in:, param1)
        assertEquals(4, countTokens(tokens, "FLOW_DOC_COMMENT_PREFIX"),
                "Should find 4 FLOW_DOC_COMMENT_PREFIX tokens. Output:\n" + formatTokens(tokens));

        // Should complete without errors
        assertEquals(2, countTokens(tokens, "FLOW_DOC_MARKER"),
                "Should find 2 FLOW_DOC_MARKER tokens. Output:\n" + formatTokens(tokens));
    }

    record TokenInfo(IElementType type, int state, int start, int end, String text) {
        @Override
        @NotNull
        public String toString() {
            var escapedText = text.replace("\n", "\\n").replace("\t", "\\t");
            return String.format("%-25s state=%-3d [%3d-%3d]: '%s'", type, state, start, end, escapedText);
        }
    }

    private static List<TokenInfo> tokenize(String yaml) {
        return tokenize(yaml, 100);
    }

    private static List<TokenInfo> tokenize(String yaml, int maxTokens) {
        var lexer = new ConcordYAMLFlexLexer();
        lexer.start(yaml, 0, yaml.length(), 0);

        var tokens = new ArrayList<TokenInfo>();
        IElementType token;

        while ((token = lexer.getTokenType()) != null && tokens.size() < maxTokens) {
            tokens.add(new TokenInfo(
                    token,
                    lexer.getState(),
                    lexer.getTokenStart(),
                    lexer.getTokenEnd(),
                    yaml.substring(lexer.getTokenStart(), lexer.getTokenEnd())
            ));
            lexer.advance();
        }

        return tokens;
    }

    private static long countTokens(List<TokenInfo> tokens, String tokenName) {
        return tokens.stream()
                .filter(t -> t.type().toString().equals(tokenName))
                .count();
    }

    private static String formatTokens(List<TokenInfo> tokens) {
        var sb = new StringBuilder();
        tokens.forEach(t -> sb.append(t).append("\n"));
        return sb.toString();
    }
}
