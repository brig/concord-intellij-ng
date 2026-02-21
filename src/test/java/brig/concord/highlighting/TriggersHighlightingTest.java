// SPDX-License-Identifier: Apache-2.0
package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

class TriggersHighlightingTest extends HighlightingTestBase {

    @Test
    void testCronTrigger() {
        configureFromText("""
            triggers:
              - cron:
                  spec: "0 9 * * MON"
                  timezone: "America/New_York"
                  entryPoint: 5.1
                  activeProfiles:
                    - "production"
            """);

        highlight(key("triggers")).is(ConcordHighlightingColors.DSL_SECTION);
        highlight(key("/triggers[0]/cron")).is(ConcordHighlightingColors.DSL_KIND);

        highlight(key("/triggers[0]/cron/spec")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/cron/timezone")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/cron/entryPoint")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/cron/activeProfiles")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(value("/triggers[0]/cron/entryPoint")).isOnly(ConcordHighlightingColors.TARGET_IDENTIFIER, ConcordHighlightingColors.STRING);
    }

    @Test
    void testManualTrigger() {
        configureFromText("""
            triggers:
              - manual:
                  name: Deploy Dev and Test
                  entryPoint: deployDev
                  activeProfiles:
                    - "production"
                  arguments:
                    runTests: true
            """);

        highlight(key("triggers")).is(ConcordHighlightingColors.DSL_SECTION);
        highlight(key("/triggers[0]/manual")).is(ConcordHighlightingColors.DSL_KIND);

        highlight(key("/triggers[0]/manual/name")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/manual/arguments")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/manual/entryPoint")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/manual/activeProfiles")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(value("/triggers[0]/manual/entryPoint")).isOnly(ConcordHighlightingColors.TARGET_IDENTIFIER, ConcordHighlightingColors.STRING);
    }

    @Test
    void testGithubTrigger() {
        configureFromResource("/highlighting/github-trigger.concord.yaml");

        highlight(key("triggers")).is(ConcordHighlightingColors.DSL_SECTION);
        highlight(key("/triggers[0]/github")).is(ConcordHighlightingColors.DSL_KIND);

        highlight(key("/triggers[0]/github/version")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/useInitiator")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/entryPoint")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/activeProfiles")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/triggers[0]/github/arguments")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/arguments/arg")).is(ConcordHighlightingColors.USER_KEY);

        highlight(key("/triggers[0]/github/ignoreEmptyPush")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/useEventCommitId")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/triggers[0]/github/exclusive")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/exclusive/group")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/exclusive/groupBy")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/exclusive/mode")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/triggers[0]/github/conditions")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/githubRepo")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/githubOrg")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/branch")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/githubHost")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/sender")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/status")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/type")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/triggers[0]/github/conditions/repositoryInfo")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/repositoryInfo[0]/branch")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/repositoryInfo[0]/enabled")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/repositoryInfo[0]/projectId")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/repositoryInfo[0]/repository")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/repositoryInfo[0]/repositoryId")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/triggers[0]/github/conditions/files")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/files/added")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/files/any")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/files/modified")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/files/removed")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("/triggers[0]/github/conditions/payload")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/triggers[0]/github/conditions/payload/message")).is(ConcordHighlightingColors.USER_KEY);

        highlight(value("/triggers[0]/github/entryPoint")).isOnly(ConcordHighlightingColors.TARGET_IDENTIFIER, ConcordHighlightingColors.STRING);
    }
}
