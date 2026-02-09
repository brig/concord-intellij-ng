package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

class PublicFlowsHighlightingTest extends HighlightingTestBase {

    @Test
    void testPublicFlowsSection() {
        configureFromText("""
            publicFlows:
              - main
              - deploy
            """);

        highlight(key("publicFlows")).is(ConcordHighlightingColors.DSL_SECTION);

        highlight(value("publicFlows[0]")).isOnly(ConcordHighlightingColors.STRING);
    }
}
