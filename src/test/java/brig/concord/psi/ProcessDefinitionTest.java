package brig.concord.psi;

import brig.concord.ConcordYamlTestBase;
import com.intellij.openapi.application.ReadAction;
import org.junit.jupiter.api.Test;

public class ProcessDefinitionTest extends ConcordYamlTestBase {

    @Test
    public void testFindEnclosingFlowDefinition_insideStep() {
        configureFromText("""
                flows:
                  myFlow:
                    - log: "hello"
                """);

        ReadAction.run(() -> {
            var logKey = key("/flows/myFlow/[0]/log").element();
            var result = ProcessDefinition.findEnclosingFlowDefinition(logKey);

            assertNotNull(result);
            assertEquals("myFlow", result.getKeyText());
        });
    }

    @Test
    public void testFindEnclosingFlowDefinition_onFlowKey() {
        configureFromText("""
                flows:
                  myFlow:
                    - log: "hello"
                """);

        ReadAction.run(() -> {
            var flowKey = key("/flows/myFlow").element();
            var result = ProcessDefinition.findEnclosingFlowDefinition(flowKey);

            assertNotNull(result);
            assertEquals("myFlow", result.getKeyText());
        });
    }

    @Test
    public void testFindEnclosingFlowDefinition_outsideFlows() {
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

            assertNull(result);
        });
    }

    @Test
    public void testFindEnclosingFlowDefinition_deeplyNested() {
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

            assertNotNull(result);
            assertEquals("myFlow", result.getKeyText());
        });
    }

    @Test
    public void testFindEnclosingFlowDefinition_nullElement() {
        configureFromText("""
                flows:
                  myFlow:
                    - log: "hello"
                """);

        ReadAction.run(() -> {
            var result = ProcessDefinition.findEnclosingFlowDefinition(null);
            assertNull(result);
        });
    }

    @Test
    public void testFindEnclosingFlowDefinition_onFlowsKey() {
        configureFromText("""
                flows:
                  myFlow:
                    - log: "hello"
                """);

        ReadAction.run(() -> {
            var flowsKey = key("/flows").element();
            var result = ProcessDefinition.findEnclosingFlowDefinition(flowsKey);

            assertNull(result);
        });
    }

    @Test
    public void testFindEnclosingFlowDefinition_multipleFlows() {
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
            assertNotNull(firstResult);
            assertEquals("firstFlow", firstResult.getKeyText());

            var secondLogKey = key("/flows/secondFlow/[0]/log").element();
            var secondResult = ProcessDefinition.findEnclosingFlowDefinition(secondLogKey);
            assertNotNull(secondResult);
            assertEquals("secondFlow", secondResult.getKeyText());
        });
    }
}
