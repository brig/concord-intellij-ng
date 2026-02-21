// SPDX-License-Identifier: Apache-2.0
package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

class FormsHighlightingTest extends HighlightingTestBase {

    @Test
    void testFormsSection() {
        configureFromText("""
            forms:
              myForm:
                - firstName: { label: "Name", type: "string" }
            """);

        highlight(key("/forms")).is(ConcordHighlightingColors.DSL_SECTION);
        highlight(key("/forms/myForm")).is(ConcordHighlightingColors.DSL_KIND);
        highlight(key("/forms/myForm[0]/firstName")).is(ConcordHighlightingColors.DSL_KIND);
        highlight(key("/forms/myForm[0]/firstName/label")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/forms/myForm[0]/firstName/type")).is(ConcordHighlightingColors.DSL_KEY);
    }
}
