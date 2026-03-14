// SPDX-License-Identifier: Apache-2.0
package brig.concord.highlighting;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

class StepsHighlightingTest extends HighlightingTestBase {

    @Test
    void testTaskStep() {
        configureFromText("""
            flows:
              default:
                - task: http
                  name: "my http task"
                  in:
                    url: "http://localhost"
                  out:
                    code: "${result.code}"
                  ignoreErrors: true
                  loop:
                    items: [1, 2, 3]
                    mode: parallel
                    parallelism: 3
                  retry:
                    times: 1
                    delay: 10
                  meta:
                    dryRunReady: true
                  error:
                    - log: "${lastError}"
            """);

        highlight(key("flows/default[0]/task")).is(ConcordHighlightingColors.STEP_KEYWORD);
        highlight(value("flows/default[0]/task")).is(ConcordHighlightingColors.TARGET_IDENTIFIER);

        highlight(key("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(value("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_LABEL);

        highlight(key("flows/default[0]/in")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/in/url")).is(ConcordHighlightingColors.USER_KEY);

        highlight(key("flows/default[0]/out")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/out/code")).is(ConcordHighlightingColors.USER_KEY);

        highlight(key("flows/default[0]/ignoreErrors")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("flows/default[0]/loop")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/loop/items")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/loop/mode")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/loop/parallelism")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("flows/default[0]/retry")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/retry/times")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/retry/delay")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("flows/default[0]/meta")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/meta/dryRunReady")).is(ConcordHighlightingColors.USER_KEY);

        highlight(key("flows/default[0]/error")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/error[0]/log")).is(ConcordHighlightingColors.STEP_KEYWORD);
    }

    @Test
    void testCallStep() {
        configureFromText("""
            flows:
              default:
                - call: myFlow
                  name: "my flow call"
                  in:
                    url: "http://localhost"
                  out:
                    code: "${result.code}"
                  loop:
                    items: [1, 2, 3]
                    mode: parallel
                    parallelism: 3
                  retry:
                    times: 1
                    delay: 10
                  meta:
                    dryRunReady: true
                  error:
                    - log: "${lastError}"
            """);

        highlight(key("flows/default[0]/call")).is(ConcordHighlightingColors.STEP_KEYWORD);
        highlight(value("flows/default[0]/call")).is(ConcordHighlightingColors.TARGET_IDENTIFIER);

        highlight(key("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(value("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_LABEL);

        highlight(key("flows/default[0]/in")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/in/url")).is(ConcordHighlightingColors.USER_KEY);

        highlight(key("flows/default[0]/out")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/out/code")).is(ConcordHighlightingColors.USER_KEY);

        highlight(key("flows/default[0]/loop")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/loop/items")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/loop/mode")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/loop/parallelism")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("flows/default[0]/retry")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/retry/times")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/retry/delay")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("flows/default[0]/meta")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/meta/dryRunReady")).is(ConcordHighlightingColors.USER_KEY);

        highlight(key("flows/default[0]/error")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("flows/default[0]/error[0]/log")).is(ConcordHighlightingColors.STEP_KEYWORD);
    }

    @Test
    void testLogStep() {
        configureFromText("""
            flows:
              default:
                - log: "Hello world"
                  name: "My step"
                  meta:
                    myMeta: "A1"
            """);

        highlight(key("flows/default[0]/log")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(value("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_LABEL);

        highlight(key("flows/default[0]/meta")).is(ConcordHighlightingColors.DSL_KEY);
    }

    @Test
    void testLogYamlStep() {
        configureFromText("""
            flows:
              default:
                - logYaml: "Hello world"
                  name: "My step"
                  meta:
                    myMeta: "A1"
            """);

        highlight(key("flows/default[0]/logYaml")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(value("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_LABEL);

        highlight(key("flows/default[0]/meta")).is(ConcordHighlightingColors.DSL_KEY);
    }

    @Test
    void testCheckpointStep() {
        configureFromText("""
            flows:
              default:
                - checkpoint: "before"
                  meta:
                    myMeta: "A1"
            """);

        highlight(key("flows/default[0]/checkpoint")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("flows/default[0]/meta")).is(ConcordHighlightingColors.DSL_KEY);
    }

    @Test
    void testIfStep() {
        configureFromText("""
            flows:
              main:
                - if: ${condition}
                  then:
                    - log: "true branch"
                  else:
                    - log: "false branch"
            """);

        highlight(key("/flows/main[0]/if")).is(ConcordHighlightingColors.STEP_KEYWORD);
        highlight(key("/flows/main[0]/then")).is(ConcordHighlightingColors.STEP_KEYWORD);
        highlight(key("/flows/main[0]/else")).is(ConcordHighlightingColors.STEP_KEYWORD);
    }

    @Test
    void testSetStep() {
        configureFromText("""
            flows:
              main:
                - set:
                    action: "delete"
            """);

        highlight(key("/flows/main[0]/set")).is(ConcordHighlightingColors.STEP_KEYWORD);
        highlight(key("/flows/main[0]/set/action")).is(ConcordHighlightingColors.USER_KEY);
    }

    @Test
    void testThrowStep() {
        configureFromText("""
            flows:
              default:
                - throw: "BOOM"
                  name: "Happy new year"
            """);

        highlight(key("/flows/default[0]/throw")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(value("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_LABEL);
    }

    @Test
    void testThrowStepNameWithoutQ() {
        configureFromText("""
            flows:
              default:
                - throw: "BOOM"
                  name: Happy new year
            """);

        highlight(key("/flows/default[0]/throw")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(value("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_LABEL);
    }

    @Test
    void testSuspendStep() {
        configureFromText("""
            flows:
              default:
                - suspend: "SLEEP"
            """);

        highlight(key("/flows/default[0]/suspend")).is(ConcordHighlightingColors.STEP_KEYWORD);
    }

    @Test
    void testExprStep() {
        configureFromText("""
            flows:
              default:
                - expr: "${1+1}"
                  name: "Math!"
            """);

        highlight(key("/flows/default[0]/expr")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(value("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_LABEL);
    }

    @Test
    void testParallel() {
        configureFromText("""
            flows:
              default:
                - parallel:
                    - task: "task1"
                    - task: "task2"
                  out:
                    - result1
                    - result2
                  meta:
                    metaKey: metaValue
            """);

        highlight(key("/flows/default[0]/parallel")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("/flows/default[0]/parallel[0]/task")).is(ConcordHighlightingColors.STEP_KEYWORD);
        highlight(key("/flows/default[0]/parallel[1]/task")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("/flows/default[0]/out")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/flows/default[0]/meta")).is(ConcordHighlightingColors.DSL_KEY);
    }

    @Test
    void testScriptStep() {
        configureFromText("""
            flows:
              default:
                - script: groovy
                  body: |
                    println "hello"
                  name: "Example"
            """);

        highlight(key("/flows/default[0]/script")).is(ConcordHighlightingColors.STEP_KEYWORD);
        highlight(key("/flows/default[0]/body")).is(ConcordHighlightingColors.DSL_KEY);

        highlight(key("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(value("flows/default[0]/name")).is(ConcordHighlightingColors.DSL_LABEL);
    }

    @Test
    void testSwitchStep() {
        configureFromText("""
            flows:
              default:
                - switch: ${env}
                  prod:
                    - log: "production"
                  dev:
                    - log: "development"
                  default:
                    - log: "unknown"
            """);

        highlight(key("/flows/default[0]/switch")).is(ConcordHighlightingColors.STEP_KEYWORD);
        highlight(key("/flows/default[0]/default")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("/flows/default[0]/prod")).is(ConcordHighlightingColors.USER_KEY);
        highlight(key("/flows/default[0]/dev")).is(ConcordHighlightingColors.USER_KEY);
    }

    @Test
    void testTryStep() {
        configureFromText("""
            flows:
              default:
                - try:
                    - task: "riskyTask"
                  error:
                    - log: ${lastError}
            """);

        highlight(key("/flows/default[0]/try")).is(ConcordHighlightingColors.STEP_KEYWORD);
        highlight(key("/flows/default[0]/error")).is(ConcordHighlightingColors.DSL_KEY);
    }

    @Test
    void testBlock() {
        configureFromText("""
            flows:
              default:
                - block:
                    - log: "step 1"
                    - log: "step 2"
                  error:
                    - log: "error handler"
            """);

        highlight(key("/flows/default[0]/block")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("/flows/default[0]/block[0]/log")).is(ConcordHighlightingColors.STEP_KEYWORD);
        highlight(key("/flows/default[0]/block[1]/log")).is(ConcordHighlightingColors.STEP_KEYWORD);

        highlight(key("/flows/default[0]/error")).is(ConcordHighlightingColors.DSL_KEY);
    }

    @Test
    void testFormStep() {
        configureFromText("""
            flows:
              default:
                - form: "myForm"
                  values:
                    field1: "default"
                  yield: true
                  saveSubmittedBy: true
            """);

        highlight(key("/flows/default[0]/form")).is(ConcordHighlightingColors.STEP_KEYWORD);
        highlight(key("/flows/default[0]/values")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/flows/default[0]/values/field1")).is(ConcordHighlightingColors.USER_KEY);
        highlight(key("/flows/default[0]/yield")).is(ConcordHighlightingColors.DSL_KEY);
        highlight(key("/flows/default[0]/saveSubmittedBy")).is(ConcordHighlightingColors.DSL_KEY);
    }

    @Test
    void testReturn() {
        configureFromText("""
            flows:
              default:
                - return
            """);

        highlight(value("/flows/default[0]")).is(ConcordHighlightingColors.STEP_KEYWORD);
    }

    @Test
    void testReturnQuoted() {
        configureFromText("""
            flows:
              default:
                - "return"
            """);

        highlight(value("/flows/default[0]")).is(ConcordHighlightingColors.STEP_KEYWORD);
    }

    @Test
    void testReturnQuotedSingle() {
        configureFromText("""
            flows:
              default:
                - 'return'
            """);

        highlight(value("/flows/default[0]")).is(ConcordHighlightingColors.STEP_KEYWORD);
    }

    @Test
    void testExit() {
        configureFromText("""
            flows:
              default:
                - exit
            """);

        highlight(value("/flows/default[0]")).is(ConcordHighlightingColors.STEP_KEYWORD);
    }

    @Test
    void testCommentedOutStepNotHighlighted() {
        configureFromText("""
            flows:
              datavantPortalIngestion:
            #    - call: datavantPortalIngestionSetupDb

                - log: ""
            """);

        assertNoSemanticHighlightsInComments();
    }

    @Test
    void testCommentedOutStepWithIndent() {
        configureFromText("""
            flows:
              default:
                # - call: myFlow
                - log: "hello"
            """);

        assertNoSemanticHighlightsInComments();
    }

    @Test
    void testMultipleCommentedOutSteps() {
        configureFromText("""
            flows:
              default:
                # - call: myFlow
                # - task: http
                - log: "hello"
            """);

        assertNoSemanticHighlightsInComments();
    }

    @Test
    void testCommentedOutStepNoBlankLine() {
        configureFromText("""
            flows:
              default:
                #- call: myFlow
                - log: "hello"
            """);

        assertNoSemanticHighlightsInComments();
    }

    private void assertNoSemanticHighlightsInComments() {
        var text = myFixture.getEditor().getDocument().getText();
        var infos = myFixture.doHighlighting();

        // # inside strings would give false positives, but test fixtures don't have that
        var commentRanges = new ArrayList<int[]>();
        int idx = 0;
        while ((idx = text.indexOf('#', idx)) != -1) {
            int lineStart = text.lastIndexOf('\n', idx) + 1;
            var prefix = text.substring(lineStart, idx).trim();
            if (prefix.isEmpty()) {
                int lineEnd = text.indexOf('\n', idx);
                if (lineEnd == -1) {
                    lineEnd = text.length();
                }
                commentRanges.add(new int[]{idx, lineEnd});
            }
            idx++;
        }

        var semanticKeys = Set.of("CONCORD_STEP_KEYWORD", "CONCORD_TARGET_IDENTIFIER",
                "CONCORD_DSL_KEY", "CONCORD_USER_KEY", "CONCORD_DSL_LABEL", "CONCORD_FLOW_IDENTIFIER");
        for (var info : infos) {
            var key = HighlightAssertion.infoKey(info);
            if (key == null) {
                continue;
            }
            for (var range : commentRanges) {
                if (info.getEndOffset() > range[0] && info.getStartOffset() < range[1]) {
                    if (semanticKeys.contains(key.getExternalName())) {
                        fail("Semantic highlight in comment: " + key.getExternalName() +
                                " at [" + info.getStartOffset() + ".." + info.getEndOffset() + "]" +
                                " comment=[" + range[0] + ".." + range[1] + "]");
                    }
                }
            }
        }
    }
}
