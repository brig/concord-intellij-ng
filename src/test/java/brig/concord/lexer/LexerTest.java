// SPDX-License-Identifier: Apache-2.0
package brig.concord.lexer;

import org.junit.jupiter.api.Test;

import static brig.concord.ConcordYamlTestBaseJunit5.loadResource;
import static brig.concord.assertions.TokenAssertions.assertTokens;

class LexerTest {

    @Test
    void lexerTest1() {
        var yaml = loadResource("/lexerTests/001.concord.yaml");

        assertTokens(yaml)
                .hasCount("FLOW_DOC_MARKER", 2);
    }
}
