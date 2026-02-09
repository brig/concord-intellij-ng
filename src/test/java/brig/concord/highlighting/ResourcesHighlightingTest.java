package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

class ResourcesHighlightingTest extends HighlightingTestBase {

    @Test
    void testResourcesSection() {
        configureFromText("""
            resources:
              concord:
                - "/"
            """);

        highlight(key("resources")).is(ConcordHighlightingColors.DSL_SECTION);
        highlight(key("resources/concord")).is(ConcordHighlightingColors.DSL_KIND);

        highlight(value("resources/concord[0]")).is(ConcordHighlightingColors.STRING);
    }
}
