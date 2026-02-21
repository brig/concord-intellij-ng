// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.List;

class OkInspectionTests extends InspectionTestBase {

    @Override
    protected Collection<Class<? extends LocalInspectionTool>> enabledInspections() {
        return List.of(MissingKeysInspection.class, UnknownKeysInspection.class, ValueInspection.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/ok/checkpoint/000.concord.yml",
            "/ok/configuration/000.concord.yml",
            "/ok/expression/000.concord.yml",
            "/ok/expression/001.concord.yml",
            "/ok/flowCall/000.concord.yml",
            "/ok/flowCall/001.concord.yml",
            "/ok/flowCall/002.concord.yml",
            "/ok/flowCall/003.concord.yml",
            "/ok/flowCallInputParams/000.concord.yml",
            "/ok/flowCallInputParams/001.concord.yml",
            "/ok/formCall/000.concord.yml",
            "/ok/forms/000.concord.yml",
            "/ok/group/000.concord.yml",
            "/ok/if/000.concord.yml",
            "/ok/imports/000.concord.yml",
            "/ok/parallel/000.concord.yml",
            "/ok/profiles/000.concord.yml",
            "/ok/publicFlows/000.concord.yml",
            "/ok/resources/000.concord.yml",
            "/ok/return/000.concord.yml",
            "/ok/script/000.concord.yml",
            "/ok/setVariables/000.concord.yml",
            "/ok/switch/000.concord.yml",
            "/ok/taskCall/000.concord.yml",
            "/ok/triggers/000.concord.yml",
    })
    void testNoErrors(String resource) {
        configureFromResource(resource);
        assertNoErrors();
    }

    @Test
    void testFlowCallMultiDefinition() {
        var rootA = createFile("project-a/concord.yaml", """
                configuration:
                  runtime: concord-v2
                flows:
                  main:
                    - call: myFlow
                  myFlow:
                    - log: "Root"
                """);

        createFile("project-a/concord/a.concord.yaml", """
                flows:
                  myFlow:
                    - log: "A"
                """);

        configureFromExistingFile(rootA);

        inspection(value("/flows/main[0]/call")).expectNoWarnings();

        assertNoWarnings();
    }
}