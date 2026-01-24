package brig.concord.hierarchy;

import brig.concord.ConcordYamlTestBase;
import brig.concord.psi.ProcessDefinition;
import brig.concord.yaml.psi.YAMLKeyValue;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.search.GlobalSearchScope;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static brig.concord.psi.ProcessDefinition.findEnclosingFlowDefinition;

public class FlowCallHierarchyTest extends ConcordYamlTestBase {

    @Test
    public void testFindCallersSimple() {
        configureFromText("""
                flows:
                  callerFlow:
                    - call: targetFlow

                  targetFlow:
                    - log: "hello"
                """);

        ReadAction.run(() -> {
            var targetFlow = key("/flows/targetFlow").asKeyValue();

            var scope = GlobalSearchScope.fileScope(myFixture.getFile());
            var callers = FlowCallFinder.findCallers(targetFlow, scope);

            Assertions.assertEquals(1, callers.size());

            var caller = callers.getFirst();
            Assertions.assertEquals("targetFlow", caller.flowName());
            Assertions.assertFalse(caller.isDynamic());

            var containingFlow = findEnclosingFlowDefinition(caller.callKeyValue());
            Assertions.assertNotNull(containingFlow);
            Assertions.assertEquals("callerFlow", containingFlow.getKeyText());
        });
    }

    @Test
    public void testFindCallersMultiple() {
        configureFromText("""
                flows:
                  flow1:
                    - call: targetFlow

                  flow2:
                    - call: targetFlow

                  flow3:
                    - call: otherFlow

                  targetFlow:
                    - log: "hello"

                  otherFlow:
                    - log: "other"
                """);

        ReadAction.run(() -> {
            var targetFlow = key("/flows/targetFlow").asKeyValue();

            var scope = com.intellij.psi.search.GlobalSearchScope.fileScope(myFixture.getFile());
            var callers = FlowCallFinder.findCallers(targetFlow, scope);

            Assertions.assertEquals(2, callers.size());
            var callerNames = callers.stream()
                    .map(c -> findEnclosingFlowDefinition(c.callKeyValue()))
                    .filter(java.util.Objects::nonNull)
                    .map(YAMLKeyValue::getKeyText)
                    .sorted()
                    .toList();
            Assertions.assertEquals(List.of("flow1", "flow2"), callerNames);
        });
    }

    @Test
    public void testFindCalleesSimple() {
        configureFromText("""
                flows:
                  mainFlow:
                    - call: subFlow1
                    - call: subFlow2

                  subFlow1:
                    - log: "sub1"

                  subFlow2:
                    - log: "sub2"
                """);

        ReadAction.run(() -> {
            var mainFlow = key("/flows/mainFlow").asKeyValue();

            var callees = FlowCallFinder.findCallees(mainFlow);
            Assertions.assertEquals(2, callees.size());

            var calleeNames = callees.stream()
                    .map(FlowCallFinder.CallSite::flowName)
                    .sorted()
                    .toList();
            Assertions.assertEquals(List.of("subFlow1", "subFlow2"), calleeNames);
        });
    }

    @Test
    public void testFindCalleesNested() {
        configureFromText("""
                flows:
                  mainFlow:
                    - try:
                        - call: tryFlow
                      error:
                        - call: errorFlow
                    - parallel:
                        - call: parallelFlow1
                        - call: parallelFlow2

                  tryFlow:
                    - log: "try"

                  errorFlow:
                    - log: "error"

                  parallelFlow1:
                    - log: "p1"

                  parallelFlow2:
                    - log: "p2"
                """);

        ReadAction.run(() -> {
            var mainFlow = key("/flows/mainFlow").asKeyValue();

            var callees = FlowCallFinder.findCallees(mainFlow);
            Assertions.assertEquals(4, callees.size());

            var calleeNames = callees.stream()
                    .map(FlowCallFinder.CallSite::flowName)
                    .sorted()
                    .toList();
            Assertions.assertEquals(
                    List.of("errorFlow", "parallelFlow1", "parallelFlow2", "tryFlow"),
                    calleeNames
            );
        });
    }

    @Test
    public void testFindCalleesInSwitchStep() {
        configureFromText("""
                flows:
                  mainFlow:
                    - switch: ${action}
                      case1:
                        - call: flow1
                      case2:
                        - call: flow2
                      default:
                        - call: defaultFlow

                  flow1:
                    - log: "flow1"

                  flow2:
                    - log: "flow2"

                  defaultFlow:
                    - log: "default"
                """);

        ReadAction.run(() -> {
            var mainFlow = key("/flows/mainFlow").asKeyValue();

            var callees = FlowCallFinder.findCallees(mainFlow);
            Assertions.assertEquals(3, callees.size());

            var calleeNames = callees.stream()
                    .map(FlowCallFinder.CallSite::flowName)
                    .sorted()
                    .toList();
            Assertions.assertEquals(
                    List.of("defaultFlow", "flow1", "flow2"),
                    calleeNames
            );
        });
    }

    @Test
    public void testDynamicExpression() {
        configureFromText("""
                flows:
                  mainFlow:
                    - call: ${dynamicFlowName}
                    - call: staticFlow

                  staticFlow:
                    - log: "static"
                """);

        ReadAction.run(() -> {
            var mainFlow = key("/flows/mainFlow").asKeyValue();
            var callees = FlowCallFinder.findCallees(mainFlow);
            Assertions.assertEquals(2, callees.size());

            var dynamicCalls = callees.stream()
                    .filter(FlowCallFinder.CallSite::isDynamic)
                    .toList();
            Assertions.assertEquals(1, dynamicCalls.size());
            Assertions.assertEquals("${dynamicFlowName}", dynamicCalls.getFirst().flowName());

            var staticCalls = callees.stream()
                    .filter(c -> !c.isDynamic())
                    .toList();
            Assertions.assertEquals(1, staticCalls.size());
            Assertions.assertEquals("staticFlow", staticCalls.getFirst().flowName());
        });
    }

    @Test
    public void testFindContainingFlow() {
        configureFromText("""
                flows:
                  outerFlow:
                    - call: innerFlow
                    - log: "outer"

                  innerFlow:
                    - log: "inner"
                """);

        ReadAction.run(() -> {
            var outerFlow = key("/flows/outerFlow").asKeyValue();

            var callees = FlowCallFinder.findCallees(outerFlow);
            var callSite = callees.getFirst();

            var containingFlow = findEnclosingFlowDefinition(callSite.callKeyValue());
            Assertions.assertNotNull(containingFlow);
            Assertions.assertEquals("outerFlow", containingFlow.getKeyText());
        });
    }

    @Test
    public void testResolveCallToFlow() {
        configureFromText("""
                flows:
                  mainFlow:
                    - call: targetFlow

                  targetFlow:
                    - log: "target"
                """);

        ReadAction.run(() -> {
            var mainFlow = key("/flows/mainFlow").asKeyValue();

            var callees = FlowCallFinder.findCallees(mainFlow);
            Assertions.assertEquals(1, callees.size());

            var callSite = callees.getFirst();
            var resolved = FlowCallFinder.resolveCallToFlow(callSite);
            Assertions.assertNotNull(resolved);
            Assertions.assertEquals("targetFlow", resolved.getKeyText());
        });
    }

    @Test
    public void testCallerTreeStructure() {
        configureFromText("""
                flows:
                  caller1:
                    - call: target

                  caller2:
                    - call: target

                  target:
                    - log: "target"
                """);

        ReadAction.run(() -> {
            var targetFlow = key("/flows/target").asKeyValue();

            var structure = new FlowCallerTreeStructure(getProject(), targetFlow);
            var root = structure.getRootElement();
            Assertions.assertNotNull(root);

            var children = structure.getChildElements(root);
            Assertions.assertEquals(2, children.length);
        });
    }

    @Test
    public void testCalleeTreeStructure() {
        configureFromText("""
                flows:
                  main:
                    - call: sub1
                    - call: sub2

                  sub1:
                    - log: "sub1"

                  sub2:
                    - log: "sub2"
                """);

        ReadAction.run(() -> {
            var mainFlow = key("/flows/main").asKeyValue();

            var structure = new FlowCalleeTreeStructure(getProject(), mainFlow);
            var root = structure.getRootElement();
            Assertions.assertNotNull(root);

            var children = structure.getChildElements(root);
            Assertions.assertEquals(2, children.length);
        });
    }

    @Test
    public void testHierarchyProviderTarget() {
        configureFromText("""
                flows:
                  testFlow:
                    - log: "test"
                """);

        ReadAction.run(() -> {
            var flowKey = yamlPath.keyElement("/flows/testFlow");
            var flowKv = (YAMLKeyValue) flowKey.getParent();

            var provider = new FlowCallHierarchyProvider();
            Assertions.assertTrue(ProcessDefinition.isFlowDefinition(flowKv));
        });
    }
}
