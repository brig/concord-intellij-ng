package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

public class ProfilesHighlightingTest extends HighlightingTestBase {

    @Test
    public void testFormsSection() {
        configureFromText("""
            profiles:
              p1:
                forms:
                  myForm:
                    - firstName: { label: "Name", type: "string" }
                flows:
                  default:
                    - throw: "BOOM"
                configuration:
                  arguments:
                    k: "v"
            """);

        highlight(key("/profiles")).is(ConcordHighlightingColors.DSL_SECTION);
        highlight(key("/profiles/p1")).is(ConcordHighlightingColors.DSL_KIND);

        highlight(key("/profiles/p1/forms")).is(ConcordHighlightingColors.DSL_SECTION);
        highlight(key("/profiles/p1/forms/myForm")).is(ConcordHighlightingColors.DSL_KIND);
        highlight(key("/profiles/p1/forms/myForm[0]/firstName")).is(ConcordHighlightingColors.DSL_KIND);
        highlight(key("/profiles/p1/forms/myForm[0]/firstName/label")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/profiles/p1/forms/myForm[0]/firstName/type")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/profiles/p1/flows/default")).is(ConcordHighlightingColors.FLOW_IDENTIFIER);
        highlight(key("/profiles/p1/flows/default[0]/throw")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("/profiles/p1/configuration")).is(ConcordHighlightingColors.DSL_SECTION);
        highlight(key("/profiles/p1/configuration/arguments")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/profiles/p1/configuration/arguments/k")).is(ConcordHighlightingColors.USER_KEY);
    }
}
