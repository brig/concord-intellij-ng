package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

class ConfigurationHighlightingTest extends HighlightingTestBase {

    @Test
    void testConfigurationSection() {
        configureFromText("""
            configuration:
              runtime: "concord-v2"
            """);

        highlight(key("configuration")).is(ConcordHighlightingColors.DSL_SECTION);
    }

    @Test
    void testUserKeyInArguments() {
        configureFromText("""
            configuration:
              arguments:
                myVariable: "value"
                anotherVar: 123
            """);

        highlight(key("/configuration/arguments/myVariable")).is(ConcordHighlightingColors.USER_KEY);
        highlight(key("/configuration/arguments/anotherVar")).is(ConcordHighlightingColors.USER_KEY);
    }

    @Test
    void testFullConfiguration() {
        configureFromResource("/highlighting/configuration.concord.yaml");

        highlight(key("configuration")).is(ConcordHighlightingColors.DSL_SECTION);

        highlight(key("/configuration/runtime")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/debug")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/template")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/suspendTimeout")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/parallelLoopParallelism")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/configuration/exclusive")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/exclusive/group")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/exclusive/mode")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/configuration/arguments")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/arguments/k")).is(ConcordHighlightingColors.USER_KEY);

        highlight(key("/configuration/entryPoint")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/dependencies")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/processTimeout")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/configuration/requirements")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/requirements/key")).is(ConcordHighlightingColors.USER_KEY);

        highlight(key("/configuration/out")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/configuration/meta")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/meta/k")).is(ConcordHighlightingColors.USER_KEY);

        highlight(key("/configuration/events")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/recordEvents")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/recordTaskInVars")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/recordTaskMeta")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/recordTaskOutVars")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/truncateMeta")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/truncateInVars")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/truncateOutVars")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/truncateMaxArrayLength")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/truncateMaxDepth")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/truncateMaxStringLength")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/inVarsBlacklist")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/metaBlacklist")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/configuration/events/outVarsBlacklist")).is(ConcordHighlightingColors.DSL_KEY);
    }
}
