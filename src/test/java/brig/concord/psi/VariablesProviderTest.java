package brig.concord.psi;

import brig.concord.ConcordYamlTestBaseJunit5;
import brig.concord.psi.VariablesProvider.Variable;
import brig.concord.psi.VariablesProvider.VariableSource;
import brig.concord.schema.BuiltInVarsSchema;
import brig.concord.schema.SchemaType;
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

        assertInstanceOf(SchemaType.Scalar.class, argVars.get("strArg"));
        assertEquals("string", ((SchemaType.Scalar) argVars.get("strArg")).typeName());

        assertInstanceOf(SchemaType.Scalar.class, argVars.get("intArg"));
        assertEquals("integer", ((SchemaType.Scalar) argVars.get("intArg")).typeName());

        assertInstanceOf(SchemaType.Scalar.class, argVars.get("boolArg"));
        assertEquals("boolean", ((SchemaType.Scalar) argVars.get("boolArg")).typeName());

        assertInstanceOf(SchemaType.Object.class, argVars.get("objArg"));
        var objType = (SchemaType.Object) argVars.get("objArg");
        var nestedProps = objType.section().properties();
        assertEquals(Set.of("nested", "count"), nestedProps.keySet());
        assertEquals("string", ((SchemaType.Scalar) nestedProps.get("nested").schemaType()).typeName());
        assertEquals("integer", ((SchemaType.Scalar) nestedProps.get("count").schemaType()).typeName());

        assertInstanceOf(SchemaType.Array.class, argVars.get("arrArg"));

        assertInstanceOf(SchemaType.Any.class, argVars.get("exprArg"));

        assertInstanceOf(SchemaType.Scalar.class, argVars.get("plainArg"));
        assertEquals("string", ((SchemaType.Scalar) argVars.get("plainArg")).typeName());

        assertInstanceOf(SchemaType.Scalar.class, argVars.get("boolYes"));
        assertEquals("string", ((SchemaType.Scalar) argVars.get("boolYes")).typeName());
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

        assertInstanceOf(SchemaType.Scalar.class, setVars.get("strVar"));
        assertEquals("string", ((SchemaType.Scalar) setVars.get("strVar")).typeName());

        assertInstanceOf(SchemaType.Scalar.class, setVars.get("intVar"));
        assertEquals("integer", ((SchemaType.Scalar) setVars.get("intVar")).typeName());

        assertInstanceOf(SchemaType.Scalar.class, setVars.get("boolVar"));
        assertEquals("boolean", ((SchemaType.Scalar) setVars.get("boolVar")).typeName());

        assertInstanceOf(SchemaType.Object.class, setVars.get("objVar"));
        var objType = (SchemaType.Object) setVars.get("objVar");
        var nestedProps = objType.section().properties();
        assertEquals(Set.of("nested", "count"), nestedProps.keySet());
        assertEquals("string", ((SchemaType.Scalar) nestedProps.get("nested").schemaType()).typeName());
        assertEquals("integer", ((SchemaType.Scalar) nestedProps.get("count").schemaType()).typeName());

        assertInstanceOf(SchemaType.Array.class, setVars.get("arrVar"));

        assertInstanceOf(SchemaType.Any.class, setVars.get("exprVar"));

        assertInstanceOf(SchemaType.Scalar.class, setVars.get("plainStr"));
        assertEquals("string", ((SchemaType.Scalar) setVars.get("plainStr")).typeName());

        assertInstanceOf(SchemaType.Scalar.class, setVars.get("boolYes"));
        assertEquals("string", ((SchemaType.Scalar) setVars.get("boolYes")).typeName());

        assertInstanceOf(SchemaType.Scalar.class, setVars.get("boolOff"));
        assertEquals("string", ((SchemaType.Scalar) setVars.get("boolOff")).typeName());

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
        assertInstanceOf(SchemaType.Scalar.class, processedVar.schema().schemaType());
        assertEquals("integer", ((SchemaType.Scalar) processedVar.schema().schemaType()).typeName());
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
        assertEquals("integer", ((SchemaType.Scalar) outVars.get("processed").schema().schemaType()).typeName());

        assertNotNull(outVars.get("failed").schema());
        assertEquals("string", ((SchemaType.Scalar) outVars.get("failed").schema().schemaType()).typeName());
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

        var resVar = vars.stream()
                .filter(v -> v.source() == VariableSource.STEP_OUT && "res".equals(v.name()))
                .findFirst().orElseThrow();

        assertNull(resVar.schema());
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
}
