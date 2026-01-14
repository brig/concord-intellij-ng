package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

public class PublicFlowsHighlightingTest extends HighlightingTestBase {

    @Test
    public void testPublicFlowsSection() {
        configureFromText("""
            publicFlows:
              - main
              - deploy
            """);

        highlight(key("publicFlows")).is(ConcordHighlightingColors.DSL_SECTION);

        highlight(value("publicFlows[0]")).isOnly(ConcordHighlightingColors.STRING);
    }
}
