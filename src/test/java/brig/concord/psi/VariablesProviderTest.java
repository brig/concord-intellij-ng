package brig.concord.psi;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.psi.VariablesProvider.Variable;
import brig.concord.psi.VariablesProvider.VariableSource;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class VariablesProviderTest extends ConcordYamlTestBaseJunit5 {

    @Test
    void testBuiltInVarsAlwaysPresent() {
        configureFromText("""
                flows:
                  main:
                    - log: "hello"
                """);

        var target = element("/flows/main/[0]");
        var vars = VariablesProvider.getVariables(target);

        var builtInNames = ConcordBuiltInVars.VARS.stream()
                .map(ConcordBuiltInVars.BuiltInVar::name)
                .collect(Collectors.toSet());

        var actualBuiltIn = vars.stream()
                .filter(v -> v.source() == VariableSource.BUILT_IN)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(builtInNames, actualBuiltIn);
    }

    @Test
    void testArgumentsFromScope() {
        createFile("concord.yaml", """
                configuration:
                  arguments:
                    myVar: "hello"
                    count: 42
                flows:
                  main:
                    - log: "hi"
                """);

        configureFromText("""
                configuration:
                  arguments:
                    myVar: "hello"
                    count: 42
                flows:
                  main:
                    - log: "hi"
                """);

        var target = element("/flows/main/[0]");
        var vars = VariablesProvider.getVariables(target);

        var argVars = vars.stream()
                .filter(v -> v.source() == VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertTrue(argVars.contains("myVar"));
        assertTrue(argVars.contains("count"));
    }

    @Test
    void testFlowDocInParams() {
        configureFromText("""
                flows:
                  ##
                  # Do something
                  # in:
                  #   bucket: string, mandatory, The bucket
                  #   prefix: string, optional, The prefix
                  ##
                  myFlow:
                    - log: "hi"
                """);

        var target = element("/flows/myFlow/[0]");
        var vars = VariablesProvider.getVariables(target);

        var flowParams = vars.stream()
                .filter(v -> v.source() == VariableSource.FLOW_PARAMETER)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("bucket", "prefix"), flowParams);
    }

    @Test
    void testSetStep() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        x: 1
                        y: "hello"
                    - log: "${x}"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var setVars = vars.stream()
                .filter(v -> v.source() == VariableSource.SET_STEP)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("x", "y"), setVars);
    }

    @Test
    void testTaskOutScalar() {
        configureFromText("""
                flows:
                  main:
                    - task: myTask
                      out: result
                    - log: "${result}"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var outVars = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("result"), outVars);
    }

    @Test
    void testCallOutScalar() {
        configureFromText("""
                flows:
                  main:
                    - call: helper
                      out: var1
                    - log: "${var1}"
                  helper:
                    - log: "hi"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var outVars = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("var1"), outVars);
    }

    @Test
    void testCallOutArray() {
        configureFromText("""
                flows:
                  main:
                    - call: helper
                      out:
                        - var1
                        - var2
                    - log: "${var1}"
                  helper:
                    - log: "hi"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var outVars = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("var1", "var2"), outVars);
    }

    @Test
    void testCallOutMapping() {
        configureFromText("""
                flows:
                  main:
                    - call: helper
                      out:
                        k1: "${result1}"
                        k2: "${result2}"
                    - log: "${k1}"
                  helper:
                    - log: "hi"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var outVars = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("k1", "k2"), outVars);
    }

    @Test
    void testOnlyPrecedingSteps() {
        configureFromText("""
                flows:
                  main:
                    - log: "first"
                    - set:
                        after: "value"
                """);

        var target = element("/flows/main/[0]");
        var vars = VariablesProvider.getVariables(target);

        var setVars = vars.stream()
                .filter(v -> v.source() == VariableSource.SET_STEP)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertTrue(setVars.isEmpty(), "Variables from steps after current position should not be visible");
    }

    @Test
    void testNestedBlock() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        outerVar: "hello"
                    - block:
                        - log: "${outerVar}"
                """);

        var target = element("/flows/main/[1]/block/[0]");
        var vars = VariablesProvider.getVariables(target);

        var setVars = vars.stream()
                .filter(v -> v.source() == VariableSource.SET_STEP)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("outerVar"), setVars);
    }

    @Test
    void testBlockOut() {
        configureFromText("""
                flows:
                  main:
                    - block:
                        - log: "inside"
                      out: blockResult
                    - log: "${blockResult}"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var outVars = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("blockResult"), outVars);
    }

    @Test
    void testIfThenElse() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        beforeIf: "value"
                    - if: "${true}"
                      then:
                        - log: "${beforeIf}"
                """);

        var target = element("/flows/main/[1]/then/[0]");
        var vars = VariablesProvider.getVariables(target);

        var setVars = vars.stream()
                .filter(v -> v.source() == VariableSource.SET_STEP)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("beforeIf"), setVars);
    }

    @Test
    void testScriptOut() {
        configureFromText("""
                flows:
                  main:
                    - script: js
                      body: "var x = 1;"
                      out: scriptResult
                    - log: "${scriptResult}"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var outVars = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("scriptResult"), outVars);
    }

    @Test
    void testExprOut() {
        configureFromText("""
                flows:
                  main:
                    - expr: "${1 + 1}"
                      out: exprResult
                    - log: "${exprResult}"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var outVars = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("exprResult"), outVars);
    }

    @Test
    void testCollectFromStepEdgeCases() {
        configureFromText("""
                flows:
                  main:
                    - task: myTask
                      in:
                        key:
                          - set:
                              notAset1: notAvariable1
                          - set:
                              notAset2: notAvariable2
                    - log: "test"
                """);

        var target = element("/flows/main/[0]/in/key[1]/set");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of(), allNonBuiltIn);
    }

    @Test
    void testDeeplyNestedBlocks() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        outerVar: "hello"
                    - block:
                        - set:
                            midVar: "world"
                        - block:
                            - set:
                                innerVar: "!"
                            - log: "${outerVar} ${midVar} ${innerVar}"
                        - set:
                            midVar2: "world2"
                """);

        var target = element("/flows/main/[1]/block/[1]/block/[1]");
        var vars = VariablesProvider.getVariables(target);

        var setVars = vars.stream()
                .filter(v -> v.source() == VariableSource.SET_STEP)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("outerVar", "midVar", "innerVar"), setVars);
    }

    @Test
    void testCollectFromStepAndAfterSteps() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        var1: value1
                    - log: "test"
                    - set:
                        var2: value2
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("var1"), allNonBuiltIn);
    }

    @Test
    void testCollectFromIfStep() {
        configureFromText("""
                flows:
                  main:
                    - if ${true}:
                      then:
                        - set:
                            var1: value1
                      else:
                        - set:
                            var2: value1
                    - log: "var1"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("var1"), allNonBuiltIn);
    }
}
