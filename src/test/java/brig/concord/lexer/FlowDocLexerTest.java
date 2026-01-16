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

        var tokens = tokenize(yaml);

        assertTrue(countTokens(tokens, "FLOW_DOC_MARKER") > 0,
                "Should find FLOW_DOC_MARKER tokens. Output:\n" + formatTokens(tokens));
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
              ##
              processS3:
                - task: s3
            """;

        var tokens = tokenize(yaml);
        var markerCount = countTokens(tokens, "FLOW_DOC_MARKER");

        assertEquals(2, markerCount,
                "Should find 2 FLOW_DOC_MARKER tokens (1 open + 1 close). Output:\n" + formatTokens(tokens));
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
