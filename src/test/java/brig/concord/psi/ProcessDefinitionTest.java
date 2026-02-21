// SPDX-License-Identifier: Apache-2.0
package brig.concord.psi;

import brig.concord.ConcordYamlTestBaseJunit5;
import com.intellij.openapi.application.ReadAction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProcessDefinitionTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testFindEnclosingFlowDefinition_insideStep() {
        configureFromText("""
                flows:
                  myFlow:
                    - log: "hello"
                """);

        ReadAction.run(() -> {
            var logKey = key("/flows/myFlow/[0]/log").element();
            var result = ProcessDefinition.findEnclosingFlowDefinition(logKey);

            Assertions.assertNotNull(result);
            Assertions.assertEquals("myFlow", result.getKeyText());
        });
    }

    @Test
    void testFindEnclosingFlowDefinition_onFlowKey() {
        configureFromText("""
                flows:
                  myFlow:
                    - log: "hello"
                """);

        ReadAction.run(() -> {
            var flowKey = key("/flows/myFlow").element();
            var result = ProcessDefinition.findEnclosingFlowDefinition(flowKey);

            Assertions.assertNotNull(result);
            Assertions.assertEquals("myFlow", result.getKeyText());
        });
    }

    @Test
    void testFindEnclosingFlowDefinition_outsideFlows() {
        configureFromText("""
                configuration:
                  runtime: concord-v2
                flows:
                  myFlow:
                    - log: "hello"
                """);

        ReadAction.run(() -> {
            var configKey = key("/configuration/runtime").element();
            var result = ProcessDefinition.findEnclosingFlowDefinition(configKey);

            Assertions.assertNull(result);
        });
    }

    @Test
    void testFindEnclosingFlowDefinition_deeplyNested() {
        configureFromText("""
                flows:
                  myFlow:
                    - if: ${condition}
                      then:
                        - task: log
                          in:
                            msg: "nested"
                """);

        ReadAction.run(() -> {
            var msgKey = key("/flows/myFlow/[0]/then/[0]/in/msg").element();
            var result = ProcessDefinition.findEnclosingFlowDefinition(msgKey);

            Assertions.assertNotNull(result);
            Assertions.assertEquals("myFlow", result.getKeyText());
        });
    }

    @Test
    void testFindEnclosingFlowDefinition_nullElement() {
        configureFromText("""
                flows:
                  myFlow:
                    - log: "hello"
                """);

        ReadAction.run(() -> {
            var result = ProcessDefinition.findEnclosingFlowDefinition(null);
            Assertions.assertNull(result);
        });
    }

    @Test
    void testFindEnclosingFlowDefinition_onFlowsKey() {
        configureFromText("""
                flows:
                  myFlow:
                    - log: "hello"
                """);

        ReadAction.run(() -> {
            var flowsKey = key("/flows").element();
            var result = ProcessDefinition.findEnclosingFlowDefinition(flowsKey);

            Assertions.assertNull(result);
        });
    }

    @Test
    void testFindEnclosingFlowDefinition_multipleFlows() {
        configureFromText("""
                flows:
                  firstFlow:
                    - log: "first"
                  secondFlow:
                    - log: "second"
                """);

        ReadAction.run(() -> {
            var firstLogKey = key("/flows/firstFlow/[0]/log").element();
            var firstResult = ProcessDefinition.findEnclosingFlowDefinition(firstLogKey);
            Assertions.assertNotNull(firstResult);
            Assertions.assertEquals("firstFlow", firstResult.getKeyText());

            var secondLogKey = key("/flows/secondFlow/[0]/log").element();
            var secondResult = ProcessDefinition.findEnclosingFlowDefinition(secondLogKey);
            Assertions.assertNotNull(secondResult);
            Assertions.assertEquals("secondFlow", secondResult.getKeyText());
        });
    }
}
