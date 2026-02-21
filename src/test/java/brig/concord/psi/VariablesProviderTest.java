// SPDX-License-Identifier: Apache-2.0
// Copyright 2026 Concord Plugin Authors
package brig.concord.psi;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.schema.BuiltInVarsSchema;
import brig.concord.schema.SchemaType;
import brig.concord.schema.TaskSchemaRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class VariablesProviderTest extends ConcordYamlTestBaseJunit5 {

    @Override
    @BeforeEach
    protected void setUp() throws Exception {
        super.setUp();

        var registry = TaskSchemaRegistry.getInstance(getProject());
        registry.setProvider(taskName -> {
            var path = "/taskSchema/" + taskName + ".schema.json";
            return VariablesProviderTest.class.getResourceAsStream(path);
        });
    }

    @Test
    void testBuiltInVarsAlwaysPresent() {
        configureFromText("""
                flows:
                  main:
                    - log: "hello"
                """);

        var target = element("/flows/main/[0]");
        var vars = VariablesProvider.getVariables(target);

        var builtInNames = BuiltInVarsSchema.getInstance().getBuiltInVars().properties().keySet();

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
    void testArgumentSchemaInference() {
        createFile("concord.yaml", """
                configuration:
                  arguments:
                    strArg: "hello"
                    intArg: 42
                    boolArg: true
                    objArg:
                      nested: "value"
                      count: 10
                    arrArg:
                      - one
                      - two
                    exprArg: "${myExpression}"
                    plainArg: hello
                    boolYes: yes
                flows:
                  main:
                    - log: "hi"
                """);

        configureFromText("""
                configuration:
                  arguments:
                    strArg: "hello"
                    intArg: 42
                    boolArg: true
                    objArg:
                      nested: "value"
                      count: 10
                    arrArg:
                      - one
                      - two
                    exprArg: "${myExpression}"
                    plainArg: hello
                    boolYes: yes
                flows:
                  main:
                    - log: "hi"
                """);

        var target = element("/flows/main/[0]");
        var vars = VariablesProvider.getVariables(target);

        var argVars = vars.stream()
                .filter(v -> v.source() == VariableSource.ARGUMENT)
                .collect(Collectors.toMap(Variable::name, v -> v.schema().schemaType()));

        assertScalarType("string", argVars.get("strArg"));
        assertScalarType("integer", argVars.get("intArg"));
        assertScalarType("boolean", argVars.get("boolArg"));

        assertObjectType(argVars.get("objArg"), "nested", "count");
        var objProps = ((SchemaType.Object) argVars.get("objArg")).section().properties();
        assertScalarType("string", objProps.get("nested").schemaType());
        assertScalarType("integer", objProps.get("count").schemaType());

        assertInstanceOf(SchemaType.Array.class, argVars.get("arrArg"));
        assertInstanceOf(SchemaType.Any.class, argVars.get("exprArg"));

        assertScalarType("string", argVars.get("plainArg"));
        assertScalarType("string", argVars.get("boolYes"));
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
    void testSetStepSchemaInference() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        strVar: "hello"
                        intVar: 42
                        boolVar: true
                        objVar:
                          nested: "value"
                          count: 10
                        arrVar:
                          - one
                          - two
                        exprVar: "${myExpression}"
                        plainStr: hello
                        boolYes: yes
                        boolOff: off
                        nullVar:
                    - log: "test"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var setVars = vars.stream()
                .filter(v -> v.source() == VariableSource.SET_STEP)
                .collect(Collectors.toMap(Variable::name, v -> v.schema().schemaType()));

        assertScalarType("string", setVars.get("strVar"));
        assertScalarType("integer", setVars.get("intVar"));
        assertScalarType("boolean", setVars.get("boolVar"));

        assertObjectType(setVars.get("objVar"), "nested", "count");
        var objProps = ((SchemaType.Object) setVars.get("objVar")).section().properties();
        assertScalarType("string", objProps.get("nested").schemaType());
        assertScalarType("integer", objProps.get("count").schemaType());

        assertInstanceOf(SchemaType.Array.class, setVars.get("arrVar"));
        assertInstanceOf(SchemaType.Any.class, setVars.get("exprVar"));

        assertScalarType("string", setVars.get("plainStr"));
        assertScalarType("string", setVars.get("boolYes"));
        assertScalarType("string", setVars.get("boolOff"));

        assertInstanceOf(SchemaType.Any.class, setVars.get("nullVar"));
    }

    @Test
    void testTaskOutScalar() {
        configureFromText("""
                flows:
                  main:
                    - task: myTask
                      out: res
                    - log: "${res}"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var outVars = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("res"), outVars);
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
    void testCallOutScalarWithFlowDoc() {
        configureFromText("""
                flows:
                  main:
                    - call: helper
                      out: processed
                    - log: "${processed}"
                  ##
                  # Helper flow
                  # out:
                  #   processed: int, optional, Files processed count
                  #   failed: string, optional, Error message
                  ##
                  helper:
                    - log: "hi"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var processedVar = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT && "processed".equals(v.name()))
                .findFirst().orElseThrow();

        assertNotNull(processedVar.schema());
        assertScalarType("integer", processedVar.schema().schemaType());
        assertEquals("Files processed count", processedVar.schema().description());
    }

    @Test
    void testCallOutArrayWithFlowDoc() {
        configureFromText("""
                flows:
                  main:
                    - call: helper
                      out:
                        - processed
                        - failed
                    - log: "${processed}"
                  ##
                  # Helper flow
                  # out:
                  #   processed: int, optional, Files processed count
                  #   failed: string, optional, Error message
                  ##
                  helper:
                    - log: "hi"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var outVars = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT)
                .collect(Collectors.toMap(Variable::name, v -> v));

        assertEquals(Set.of("processed", "failed"), outVars.keySet());

        assertNotNull(outVars.get("processed").schema());
        assertScalarType("integer", outVars.get("processed").schema().schemaType());

        assertNotNull(outVars.get("failed").schema());
        assertScalarType("string", outVars.get("failed").schema().schemaType());
    }

    @Test
    void testCallOutMappingWithFlowDoc() {
        configureFromText("""
                flows:
                  main:
                    - call: helper
                      out:
                        localCount: "${processed}"
                        localMsg: "${failed}"
                    - log: "${localCount}"
                  ##
                  # Helper flow
                  # out:
                  #   processed: int, optional, Files processed count
                  #   failed: string, optional, Error message
                  ##
                  helper:
                    - log: "hi"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var outVars = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT)
                .collect(Collectors.toMap(Variable::name, v -> v));

        assertEquals(Set.of("localCount", "localMsg"), outVars.keySet());

        // mapping form: keys are local variable names, schema is any
        assertNotNull(outVars.get("localCount").schema());
        assertInstanceOf(SchemaType.Any.class, outVars.get("localCount").schema().schemaType());

        assertNotNull(outVars.get("localMsg").schema());
        assertInstanceOf(SchemaType.Any.class, outVars.get("localMsg").schema().schemaType());
    }

    @Test
    void testCallOutWithoutFlowDoc() {
        configureFromText("""
                flows:
                  main:
                    - call: helper
                      out: res
                    - log: "${res}"
                  helper:
                    - log: "hi"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var stepOutVars = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT)
                .toList();

        assertEquals(1, stepOutVars.size());
        var schema = stepOutVars.getFirst();
        assertEquals("res", schema.name());
        assertNotNull(schema.schema());
        assertEquals(SchemaType.ANY, schema.schema().schemaType());
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
                    - if: ${true}:
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

        assertEquals(Set.of("var1", "var2"), allNonBuiltIn);
    }

    @Test
    void testCollectFromIfStep2() {
        configureFromText("""
                flows:
                  main:
                    - if: ${true}
                      then:
                        - set:
                            var1: value1
                        - call: inner
                          out: innerVar
                      else:
                        - set:
                            var2: value1
                        - call: inner
                          out: innerVar
                    - log: "${var1}"
                    - log: "${var1}, ${var2}, ${innerVar}"
                
                  inner:
                    - set:
                        innerVar: "OGO"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("var1", "var2", "innerVar"), allNonBuiltIn);
    }

    @Test
    void testCollectFromSwitchStep() {
        configureFromText("""
                flows:
                  main:
                    - switch: ${'label1'}
                      label1:
                        - set:
                            var1: value1
                        - call: inner
                          out: innerVar
                      label2:
                        - set:
                            var2: value1
                        - call: inner
                          out: innerVar
                    - log: "${var1}"
                    - log: "${innerVar}"
                
                  inner:
                    - set:
                        innerVar: "OGO"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("var1", "var2", "innerVar"), allNonBuiltIn);
    }

    @Test
    void testCollectFromLoop() {
        configureFromText("""
                flows:
                  main:
                    - call: inner
                      in:
                        key: "${<caret>}"
                      loop:
                        items: [1, 2, 3]
                
                  inner:
                    - set:
                        innerVar: "OGO"
                """);

        var target = element("/flows/main/[0]/in/key");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(BuiltInVarsSchema.getInstance().getLoopVars().properties().keySet(), allNonBuiltIn);
    }

    @Test
    void testCollectFromStepOutsideLoop() {
        configureFromText("""
                flows:
                  main:
                    - call: inner
                      in:
                        key: "${item}"
                      loop:
                        items: [1, 2, 3]

                    - log: "${noItemHere}"
                  inner:
                    - set:
                        innerVar: "OGO"
                """);

        var target = element("/flows/main/[1]/log");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of(), allNonBuiltIn);
    }

    @Test
    void testCollectFromStepOutsideLoop2() {
        configureFromText("""
                flows:
                  main:
                    - call: inner
                      in:
                        key: "${item}"
                      loop:
                        items: [1, 2, 3]

                    - log:
                        - loop:
                            items: not a loop
                  inner:
                    - set:
                        innerVar: "OGO"
                """);

        var target = element("/flows/main/[1]/log[0]/loop/items");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of(), allNonBuiltIn);
    }

    @Test
    void testLoopInsideBlock() {
        configureFromText("""
                flows:
                  main:
                    - block:
                        - log: "${item}"
                      loop:
                        items: [1, 2]
                """);

        var target = element("/flows/main/[0]/block/[0]");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(BuiltInVarsSchema.getInstance().getLoopVars().properties().keySet(), allNonBuiltIn);
    }

    @Test
    void testPrecedingSetAndLoop() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        x: 1
                    - call: inner
                      in:
                        key: "${x} ${item}"
                      loop:
                        items: [1, 2]

                  inner:
                    - log: "hi"
                """);

        var target = element("/flows/main/[1]/in/key");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("x", "item", "items", "itemIndex"), allNonBuiltIn);
    }

    @Test
    void testCollectFromTryError() {
        configureFromText("""
                flows:
                  main:
                    - try:
                        - set:
                            x: 1
                      error:
                        - set:
                            errVar: "oops"
                    - log: "${x} ${errVar}"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("x", "errVar"), allNonBuiltIn);
    }

    @Test
    void testCollectFromPrecedingBlock() {
        configureFromText("""
                flows:
                  main:
                    - block:
                        - set:
                            x: 1
                    - log: "${x}"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("x"), allNonBuiltIn);
    }

    @Test
    void testTaskResultInsideOut() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        myVar: "42"

                    - task: myTask
                      out:
                        k1: "${result.data}"
                """);

        var target = element("/flows/main/[1]/out/k1");
        var vars = VariablesProvider.getVariables(target);

        var allNonBuiltIn = vars.stream()
                .filter(v -> v.source() != VariableSource.BUILT_IN && v.source() != VariableSource.ARGUMENT)
                .map(Variable::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("myVar", "result"), allNonBuiltIn);
    }

    @Test
    void testTaskOutScalarWithSchema() {
        configureFromText("""
                flows:
                  main:
                    - task: strictTask
                      out: res
                    - log: "${res}"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var resVar = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT && "res".equals(v.name()))
                .findFirst().orElseThrow();

        assertObjectType(resVar.schema().schemaType(), "ok", "body");
        assertEquals("task result", resVar.schema().description());

        var props = ((SchemaType.Object) resVar.schema().schemaType()).section().properties();
        assertScalarType("boolean", props.get("ok").schemaType());
        assertScalarType("string", props.get("body").schemaType());
    }

    @Test
    void testTaskOutWithoutSchema() {
        configureFromText("""
                flows:
                  main:
                    - task: unknownTask
                      out: res
                    - log: "${res}"
                """);

        var target = element("/flows/main/[1]");
        var vars = VariablesProvider.getVariables(target);

        var resVar = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT && "res".equals(v.name()))
                .findFirst().orElseThrow();

        assertInstanceOf(SchemaType.Any.class, resVar.schema().schemaType());
    }

    @Test
    void testTaskResultInsideOutWithSchema() {
        configureFromText("""
                flows:
                  main:
                    - set:
                        myVar: "42"

                    - task: strictTask
                      out:
                        k1: "${result.ok}"
                """);

        var target = element("/flows/main/[1]/out/k1");
        var vars = VariablesProvider.getVariables(target);

        var resultVar = vars.stream()
                .filter(v -> v.source() == VariableSource.TASK_RESULT && "result".equals(v.name()))
                .findFirst().orElseThrow();

        assertObjectType(resultVar.schema().schemaType(), "ok", "body");
    }

    private static void assertScalarType(String expectedTypeName, SchemaType actual) {
        assertInstanceOf(SchemaType.Scalar.class, actual);
        assertEquals(expectedTypeName, ((SchemaType.Scalar) actual).typeName());
    }

    private static void assertObjectType(SchemaType actual, String... expectedKeys) {
        assertInstanceOf(SchemaType.Object.class, actual);
        var objType = (SchemaType.Object) actual;
        assertEquals(Set.of(expectedKeys), objType.section().properties().keySet());
    }
}
