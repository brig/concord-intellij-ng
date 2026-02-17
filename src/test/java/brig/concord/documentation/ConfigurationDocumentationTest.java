// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.documentation;

import org.junit.jupiter.api.Test;

public class ConfigurationDocumentationTest extends BaseDocumentationTargetTest {

    @Test
    public void testCompleteConfiguration() {
        configureFromText("""
                flows:
                  main:
                    - log: "Hello"

                configuration:
                  debug: true
                  exclusive:
                    group: "myGroup"
                    mode: cancel
                  parallelLoopParallelism: 123
                  suspendTimeout: "PT10M"
                  template: "hello"
                  runtime: concord-v2
                  arguments:
                    k: "v"
                  entryPoint: "deployApp"
                  dependencies:
                    - "mvn://"
                  processTimeout: "PT1H"
                  requirements:
                    key: "value"
                  out:
                    - "test"
                  meta:
                    k: "v"
                  events:
                    recordTaskInVars: false
                    truncateInVars: true
                    recordTaskOutVars: false
                    truncateOutVars: true
                    truncateMaxStringLength: 1024
                    truncateMaxArrayLength: 32
                    truncateMaxDepth: 10
                    inVarsBlacklist:
                      - "apiKey"
                      - "apiToken"
                      - "password"
                      - "privateKey"
                      - "vaultPassword"
                    outVarsBlacklist: []""");

        assertDocTarget(key("/configuration"), "doc.configuration.description",
                "/documentation/configuation.html");

        assertDocTarget(key("/configuration/debug"), "doc.configuration.debug.description", "/documentation/configuation.debug.html");

        assertDocTarget(key("/configuration/exclusive"), "doc.configuration.exclusive.description",
                "/documentation/configuation.exclusive.html");
        assertDocTarget(key("/configuration/exclusive/group"), "doc.configuration.exclusive.group.description",
                "/documentation/configuation.exclusive.group.html");
        assertDocTarget(key("/configuration/exclusive/mode"), "doc.configuration.exclusive.mode.description",
                "/documentation/configuation.exclusive.mode.html");

        assertDocTarget(key("/configuration/parallelLoopParallelism"), "doc.configuration.parallelLoopParallelism.description",
                "/documentation/configuation.parallelLoopParallelism.html");
        assertDocTarget(key("/configuration/suspendTimeout"), "doc.configuration.suspendTimeout.description",
                "/documentation/configuation.suspendTimeout.html");
        assertDocTarget(key("/configuration/template"), "doc.configuration.template.description",
                "/documentation/configuation.template.html");
        assertDocTarget(key("/configuration/runtime"), "doc.configuration.runtime.description",
                "/documentation/configuation.runtime.html");

        assertDocTarget(key("/configuration/arguments"), "doc.configuration.arguments.description",
                "/documentation/configuation.arguments.html");
        assertNoDocTarget(key("/configuration/arguments/k"));

        assertDocTarget(key("/configuration/entryPoint"), "doc.configuration.entryPoint.description",
                "/documentation/configuation.entryPoint.html");

        assertDocTarget(key("/configuration/dependencies"), "doc.configuration.dependencies.description",
                "/documentation/configuation.dependencies.html");
        assertNoDocTarget(value("/configuration/dependencies[0]"));

        assertDocTarget(key("/configuration/processTimeout"), "doc.configuration.processTimeout.description",
                "/documentation/configuation.processTimeout.html");

        assertDocTarget(key("/configuration/requirements"), "doc.configuration.requirements.description",
                "/documentation/configuation.requirements.html");
        assertNoDocTarget(key("/configuration/requirements/key"));
        assertNoDocTarget(value("/configuration/requirements/key"));

        assertDocTarget(key("/configuration/out"), "doc.configuration.out.description",
                "/documentation/configuation.out.html");
        assertNoDocTarget(value("/configuration/out[0]"));

        assertDocTarget(key("/configuration/meta"), "doc.configuration.meta.description",
                "/documentation/configuation.meta.html");
        assertNoDocTarget(key("/configuration/meta/k"));
        assertNoDocTarget(value("/configuration/meta/k"));

        assertDocTarget(key("/configuration/events"), "doc.configuration.events.description",
                "/documentation/configuation.events.html");

        assertDocTarget(key("/configuration/events/recordTaskInVars"), "doc.configuration.events.recordTaskInVars.description",
                "/documentation/configuation.events.recordTaskInVars.html");
        assertDocTarget(key("/configuration/events/truncateInVars"), "doc.configuration.events.truncateInVars.description",
                "/documentation/configuation.events.truncateInVars.html");
        assertDocTarget(key("/configuration/events/recordTaskOutVars"), "doc.configuration.events.recordTaskOutVars.description",
                "/documentation/configuation.events.recordTaskOutVars.html");
        assertDocTarget(key("/configuration/events/truncateOutVars"), "doc.configuration.events.truncateOutVars.description",
                "/documentation/configuation.events.truncateOutVars.html");
        assertDocTarget(key("/configuration/events/truncateMaxStringLength"), "doc.configuration.events.truncateMaxStringLength.description",
                "/documentation/configuation.events.truncateMaxStringLength.html");
        assertDocTarget(key("/configuration/events/truncateMaxArrayLength"), "doc.configuration.events.truncateMaxArrayLength.description",
                "/documentation/configuation.events.truncateMaxArrayLength.html");
        assertDocTarget(key("/configuration/events/truncateMaxDepth"), "doc.configuration.events.truncateMaxDepth.description",
                "/documentation/configuation.events.truncateMaxDepth.html");
        assertDocTarget(key("/configuration/events/inVarsBlacklist"), "doc.configuration.events.inVarsBlacklist.description",
                "/documentation/configuation.events.inVarsBlacklist.html");
        assertDocTarget(key("/configuration/events/outVarsBlacklist"), "doc.configuration.events.outVarsBlacklist.description",
                "/documentation/configuation.events.outVarsBlacklist.html");
    }
}
