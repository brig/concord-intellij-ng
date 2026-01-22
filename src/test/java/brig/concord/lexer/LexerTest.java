package brig.concord.lexer;

import org.junit.jupiter.api.Test;

import static brig.concord.ConcordYamlTestBase.loadResource;
import static brig.concord.assertions.TokenAssertions.assertTokens;

public class LexerTest {

    @Test
    public void lexerTest1() {
        var yaml = loadResource("/lexerTests/001.concord.yaml");

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2);
    }
}
